import requests
import sys

# API Configuration
BASE_URL = "http://localhost:8080/api"
TIMEOUT = 10

class ScenarioVerifier:
    def __init__(self):
        self.session = requests.Session()
        self.token = None
        self.user_data = {
            "username": "test",
            "password": "test"
        }

    def log(self, step, message):
        print(f"[STEP {step}] {message}")

    def run(self):
        try:
            # UC-U-01: Login Flow
            self.test_login_flow()
            
            # UC-D-02: Create Document
            doc_id = self.test_create_document()
            
            # UC-D-03: Read Document & Permission Check
            self.test_read_and_permissions(doc_id)

            # UC-D-05: File Attachment
            self.test_file_upload_and_download()

            # UC-A-01: Admin User Management
            self.test_admin_flow()

            # UC-U-01 (Extended): Password Change Flow
            self.test_password_flows()

            # Security Check: Regular user should not access Admin API
            self.test_admin_api_security()

            # New: Search and Sorting Scenarios
            self.test_search_scenarios()
            self.test_sorting_scenarios()
            self.test_sorting_by_author()
            self.test_paging_search()
            
            print("\n✅ All Use Case Scenarios PASSED!")
        except Exception as e:
            print(f"\n❌ Scenario FAILED: {str(e)}")
            import traceback
            traceback.print_exc()
            sys.exit(1)

    def test_login_flow(self):
        self.log("UC-U-01", "Starting Login Flow...")
        
        login_payload = {
            "username": self.user_data["username"],
            "password": self.user_data["password"]
        }
        
        # Auth is at /api/v1/auth/login
        response = self.session.post(f"{BASE_URL}/v1/auth/login", json=login_payload, timeout=TIMEOUT)
        
        if response.status_code != 200:
            self.log("UC-U-01", "Default login failed, attempting recovery from leftover password...")
            # Try the known 'new' password from tests
            recovery_payload = {"username": "test", "password": "test_password_new"}
            recovery_resp = self.session.post(f"{BASE_URL}/v1/auth/login", json=recovery_payload, timeout=TIMEOUT)
            if recovery_resp.status_code == 200:
                self.token = recovery_resp.json().get("token")
                self.session.headers.update({"Authorization": f"Bearer {self.token}"})
                # Revert to default
                self.session.put(f"{BASE_URL}/profile/password", json={"password": "test"}, timeout=TIMEOUT)
                self.log("UC-U-01", "Recovered and reverted password to default.")
                response = recovery_resp # Use this as the successful response
            else:
                assert response.status_code == 200, f"Login failed and recovery failed: {response.text}"
        
        assert response.status_code == 200, f"Login failed: {response.text}"
        self.token = response.json().get("token")
        self.session.headers.update({"Authorization": f"Bearer {self.token}"})
        self.log("UC-U-01", "Login successful, token acquired.")

    def test_create_document(self):
        self.log("UC-D-02", "Creating a new document...")
        
        payload = {
            "title": "Scenario Integration Test Doc",
            "content": "# Hello World\nThis is a real DB test.",
            "status": "PUBLISHED",
            "publicRead": True
        }
        
        response = self.session.post(f"{BASE_URL}/documents", json=payload, timeout=TIMEOUT)
        assert response.status_code == 201, f"Creation failed: {response.text}"
        
        doc = response.json()
        doc_id = doc.get("id")
        self.log("UC-D-02", f"Document created with ID: {doc_id}")
        return doc_id

    def test_read_and_permissions(self, doc_id):
        self.log("UC-D-03", "Verifying Document Detail and Permissions...")
        
        # Step: Read the document
        response = self.session.get(f"{BASE_URL}/documents/{doc_id}", timeout=TIMEOUT)
        assert response.status_code == 200, f"Read failed: {response.text}"
        assert response.json()["title"] == "Scenario Integration Test Doc"
        
        # Step: Search keyword
        search_response = self.session.get(f"{BASE_URL}/documents", params={"query": "Scenario Integration"}, timeout=TIMEOUT)
        assert search_response.status_code == 200
        titles = [d["title"] for d in search_response.json()["content"]]
        assert "Scenario Integration Test Doc" in titles
        
        self.log("UC-D-03", "Document verified in detail and search results.")

    def test_file_upload_and_download(self):
        self.log("UC-D-05", "Starting File Upload/Download Scenario...")
        
        # 1. Create a dummy file
        dummy_content = b"This is a dummy file content for testing."
        files = {'file': ('test_file.txt', dummy_content, 'text/plain')}
        
        # 2. Upload
        upload_response = self.session.post(f"{BASE_URL}/attachments/upload", files=files, timeout=TIMEOUT)
        assert upload_response.status_code == 200, f"Upload failed: {upload_response.text}"
        
        attachment = upload_response.json()
        attach_id = attachment.get("id")
        self.log("UC-D-05", f"File uploaded successfully. ID: {attach_id}")
        
        # 3. Download and Verify
        download_response = self.session.get(f"{BASE_URL}/attachments/{attach_id}", timeout=TIMEOUT)
        assert download_response.status_code == 200, f"Download failed: {download_response.text}"
        assert download_response.content == dummy_content, "Downloaded content mismatch!"
        assert "test_file.txt" in download_response.headers.get("Content-Disposition", "")
        
        self.log("UC-D-05", "File download and integrity verified.")

    def test_admin_flow(self):
        self.log("UC-A-01", "Starting Admin Flow (User Management)...")
        
        # 1. Login as Admin
        admin_session = requests.Session()
        login_payload = {"username": "admin", "password": "admin"}
        response = admin_session.post(f"{BASE_URL}/v1/auth/login", json=login_payload, timeout=TIMEOUT)
        assert response.status_code == 200, f"Admin login failed: {response.text}"
        token = response.json().get("token")
        admin_session.headers.update({"Authorization": f"Bearer {token}"})
        
        # Cleanup: Delete new_tester if exists (for idempotency)
        resp = admin_session.get(f"{BASE_URL}/admin/users", timeout=TIMEOUT)
        users_list = resp.json().get("content", [])
        existing_new_tester = next((u for u in users_list if u["username"] == "new_tester"), None)
        if existing_new_tester:
            admin_session.delete(f"{BASE_URL}/admin/users/{existing_new_tester['id']}", timeout=TIMEOUT)
            self.log("UC-A-01", "Old 'new_tester' cleaned up.")

        # 2. CRUD: Create a new user
        new_user_payload = {
            "username": "new_tester",
            "password": "raw_password_to_be_hashed", 
            "name": "New Tester",
            "role": "USER",
            "status": "ACTIVE"
        }
        create_response = admin_session.post(f"{BASE_URL}/admin/users", json=new_user_payload, timeout=TIMEOUT)
        assert create_response.status_code == 200, f"Admin failed to create user: {create_response.text}"
        new_user = create_response.json()
        new_user_id = new_user.get("id")
        self.log("UC-A-01", f"Admin created new user: {new_user.get('username')} (ID: {new_user_id})")

        # 3. List and Verify
        resp = admin_session.get(f"{BASE_URL}/admin/users", timeout=TIMEOUT)
        assert resp.status_code == 200
        # Unified Paging: admin/users now returns Page<User>
        users_list = resp.json().get("content", [])
        usernames = [u["username"] for u in users_list]
        assert "new_tester" in usernames, "Created user not found in list"
        
        # 4. Update Status (INACTIVE)
        # Note: updateUserStatus takes @RequestBody String status. Simple string might need plain text or be tricky with JSON.
        # Looking at AdminController: @RequestBody String status
        status_response = admin_session.put(f"{BASE_URL}/admin/users/{new_user_id}/status", data="INACTIVE", headers={"Content-Type": "text/plain"}, timeout=TIMEOUT)
        assert status_response.status_code == 200, f"Failed to update status: {status_response.text}"
        assert status_response.json()["status"] == "INACTIVE"
        self.log("UC-A-01", "Admin successfully updated user status to INACTIVE.")

        self.log("UC-A-01", f"Admin verified {len(users_list)} users and full management flow.")

    def test_password_flows(self):
        self.log("PWD-CHG", "Starting Password Change Scenarios...")
        
        # 1. User Self-Password Change
        self.log("PWD-CHG", "User 'test' changing their own password...")
        payload = {"password": "test_password_new"}
        resp = self.session.put(f"{BASE_URL}/profile/password", json=payload, timeout=TIMEOUT)
        assert resp.status_code == 200, f"User password change failed: {resp.text}"
        
        # Verify login with new password
        verify_session = requests.Session()
        resp = verify_session.post(f"{BASE_URL}/v1/auth/login", json={"username": "test", "password": "test_password_new"}, timeout=TIMEOUT)
        assert resp.status_code == 200, "Login with NEW password failed"
        new_token = resp.json().get("token")
        verify_session.headers.update({"Authorization": f"Bearer {new_token}"})
        
        # REVERT password back to 'test' (for idempotency)
        resp = verify_session.put(f"{BASE_URL}/profile/password", json={"password": "test"}, timeout=TIMEOUT)
        assert resp.status_code == 200, "Reverting password failed"
        self.log("PWD-CHG", "User self-change and login verified, then reverted.")

        # 2. Admin Reset User Password
        self.log("PWD-CHG", "Admin resetting 'new_tester' password...")
        # Need admin session
        admin_session = requests.Session()
        resp = admin_session.post(f"{BASE_URL}/v1/auth/login", json={"username": "admin", "password": "admin"}, timeout=TIMEOUT)
        admin_token = resp.json().get("token")
        admin_session.headers.update({"Authorization": f"Bearer {admin_token}"})
        
        # a. Fetch User List (to get ID)
        resp = admin_session.get(f"{BASE_URL}/admin/users", timeout=TIMEOUT)
        users_content = resp.json().get("content", [])
        target_user = next(u for u in users_content if u["username"] == "new_tester")
        uid = target_user["id"]
        
        # Reset via plain text body (as seen in AdminController @RequestBody String newPassword)
        reset_resp = admin_session.put(f"{BASE_URL}/admin/users/{uid}/password", data="admin_reset_123", headers={"Content-Type": "text/plain"}, timeout=TIMEOUT)
        assert reset_resp.status_code == 200, f"Admin reset failed: {reset_resp.text}"
        
        # Verify login with reset password
        resp = requests.post(f"{BASE_URL}/v1/auth/login", json={"username": "new_tester", "password": "admin_reset_123"}, timeout=TIMEOUT)
        assert resp.status_code == 200, "Login with RESET password failed"
        
        self.log("PWD-CHG", "Admin password reset and login verified.")

    def test_admin_api_security(self):
        self.log("SECURITY", "Verifying regular user cannot access Admin API...")
        
        # Current self.session is logged in as 'test' (USER role)
        response = self.session.get(f"{BASE_URL}/admin/users", timeout=TIMEOUT)
        
        # Expect 403 Forbidden
        assert response.status_code == 403, f"Security Breach! Regular user accessed Admin API: {response.status_code}"
        self.log("SECURITY", "Admin User List access DENIED as expected.")

        # Specific Case: Regular user trying to reset another user's password via Admin API
        self.log("SECURITY", "User 'test' attempting to reset another user's password via Admin API...")
        # Try to reset admin's password (typically ID 1 or 2)
        # We'll just try id 1 as a probe
        payload = "malicious_pwd"
        resp = self.session.put(f"{BASE_URL}/admin/users/1/password", data=payload, headers={"Content-Type": "text/plain"}, timeout=TIMEOUT)
        assert resp.status_code == 403, f"Security Breach! Regular user could call Password Reset API: {resp.status_code}"
        self.log("SECURITY", "Unauthorized password reset attempt BLOCKED (403 Forbidden).")
        
        self.log("SECURITY", "All security role restrictions verified.")

    def test_search_scenarios(self):
        self.log("SEARCH", "Starting Search Scenarios...")
        
        # 1. Create test documents
        docs_to_create = [
            {"title": "Alpha Unique Search", "content": "Content 1", "tags": [{"name": "test-tag-1"}]},
            {"title": "Beta Unique Search", "content": "Content 2", "tags": [{"name": "test-tag-2"}]},
            {"title": "Gamma", "content": "Content 3", "tags": [{"name": "test-tag-1"}, {"name": "test-tag-3"}]}
        ]
        
        created_ids = []
        for doc in docs_to_create:
            resp = self.session.post(f"{BASE_URL}/documents", json=doc, timeout=TIMEOUT)
            assert resp.status_code == 201
            created_ids.append(resp.json()["id"])
            
        # 2. Search by Title Keyword
        search_resp = self.session.get(f"{BASE_URL}/documents", params={"query": "Unique Search"}, timeout=TIMEOUT)
        assert search_resp.status_code == 200
        results = search_resp.json()["content"]
        titles = [d["title"] for d in results]
        assert len(results) >= 2
        assert "Alpha Unique Search" in titles
        assert "Beta Unique Search" in titles
        
        # 3. Search by Tag
        tag_search_resp = self.session.get(f"{BASE_URL}/documents", params={"tagName": "test-tag-1"}, timeout=TIMEOUT)
        assert tag_search_resp.status_code == 200
        tag_results = tag_search_resp.json()["content"]
        tag_titles = [d["title"] for d in tag_results]
        assert len(tag_results) >= 2
        assert "Alpha Unique Search" in tag_titles
        assert "Gamma" in tag_titles
        
        # 4. Negative Search
        neg_search_resp = self.session.get(f"{BASE_URL}/documents", params={"query": "NonExistentKeywordXYZ"}, timeout=TIMEOUT)
        assert neg_search_resp.status_code == 200
        assert neg_search_resp.json()["totalElements"] == 0
        
        self.log("SEARCH", "Search scenarios verified (Keyword, Tag, Negative).")
        
        # Cleanup
        for did in created_ids:
            self.session.delete(f"{BASE_URL}/documents/{did}", timeout=TIMEOUT)

    def test_sorting_scenarios(self):
        self.log("SORT", "Starting Sorting Scenarios...")
        
        # 1. Create documents in order B, A, C (with delays to ensure different createdAt)
        import time
        sort_docs = [
            {"title": "B-Sort", "content": "B content"},
            {"title": "A-Sort", "content": "A content"},
            {"title": "C-Sort", "content": "C content"}
        ]
        
        created_ids = []
        for doc in sort_docs:
            resp = self.session.post(f"{BASE_URL}/documents", json=doc, timeout=TIMEOUT)
            assert resp.status_code == 201
            created_ids.append(resp.json()["id"])
            time.sleep(0.1) # Small delay for createdAt ordering
            
        # 2. Sort by Title ASC
        resp_asc = self.session.get(f"{BASE_URL}/documents", params={"sort": "title,asc", "query": "-Sort"}, timeout=TIMEOUT)
        assert resp_asc.status_code == 200
        titles_asc = [d["title"] for d in resp_asc.json()["content"]]
        assert titles_asc == ["A-Sort", "B-Sort", "C-Sort"]
        
        # 3. Sort by Title DESC
        resp_desc = self.session.get(f"{BASE_URL}/documents", params={"sort": "title,desc", "query": "-Sort"}, timeout=TIMEOUT)
        assert resp_desc.status_code == 200
        titles_desc = [d["title"] for d in resp_desc.json()["content"]]
        assert titles_desc == ["C-Sort", "B-Sort", "A-Sort"]
        
        # 4. Sort by CreatedAt DESC
        # Sequence of creation was B (oldest) -> A -> C (newest)
        # DESC order should be C -> A -> B
        resp_time = self.session.get(f"{BASE_URL}/documents", params={"sort": "createdAt,desc", "query": "-Sort"}, timeout=TIMEOUT)
        assert resp_time.status_code == 200
        titles_time = [d["title"] for d in resp_time.json()["content"]]
        assert titles_time == ["C-Sort", "A-Sort", "B-Sort"]
        
        self.log("SORT", "Sorting scenarios verified (Title ASC/DESC, CreatedAt DESC).")
        
        # Cleanup
        for did in created_ids:
            self.session.delete(f"{BASE_URL}/documents/{did}", timeout=TIMEOUT)

    def test_sorting_by_author(self):
        self.log("SORT-AUTH", "Starting Author Sorting Scenarios...")
        
        # 1. Admin session setup
        admin_session = requests.Session()
        admin_resp = admin_session.post(f"{BASE_URL}/v1/auth/login", json={"username": "admin", "password": "admin"}, timeout=TIMEOUT)
        assert admin_resp.status_code == 200
        admin_token = admin_resp.json().get("token")
        admin_session.headers.update({"Authorization": f"Bearer {admin_token}"})
        
        # 2. Create documents with different authors
        # User 'test' doc
        test_doc_resp = self.session.post(f"{BASE_URL}/documents", json={"title": "Test-Auth-Doc", "content": "test"}, timeout=TIMEOUT)
        assert test_doc_resp.status_code == 201
        test_doc_id = test_doc_resp.json()["id"]
        
        # Admin doc
        admin_doc_resp = admin_session.post(f"{BASE_URL}/documents", json={"title": "Admin-Auth-Doc", "content": "admin"}, timeout=TIMEOUT)
        assert admin_doc_resp.status_code == 201
        admin_doc_id = admin_doc_resp.json()["id"]
        
        try:
            # 3. Sort by Author Username ASC
            # 'admin' < 'test'
            resp_asc = self.session.get(f"{BASE_URL}/documents", params={"sort": "author.username,asc", "query": "-Auth-Doc"}, timeout=TIMEOUT)
            assert resp_asc.status_code == 200
            titles_asc = [d["title"] for d in resp_asc.json()["content"]]
            assert titles_asc == ["Admin-Auth-Doc", "Test-Auth-Doc"]
            
            # 4. Sort by Author Username DESC
            # 'test' > 'admin'
            resp_desc = self.session.get(f"{BASE_URL}/documents", params={"sort": "author.username,desc", "query": "-Auth-Doc"}, timeout=TIMEOUT)
            assert resp_desc.status_code == 200
            titles_desc = [d["title"] for d in resp_desc.json()["content"]]
            assert titles_desc == ["Test-Auth-Doc", "Admin-Auth-Doc"]
            
            self.log("SORT-AUTH", "Author sorting scenarios verified (ASC/DESC).")
            
        finally:
            # Cleanup
            self.session.delete(f"{BASE_URL}/documents/{test_doc_id}", timeout=TIMEOUT)
            admin_session.delete(f"{BASE_URL}/documents/{admin_doc_id}", timeout=TIMEOUT)

    def test_paging_search(self):
        self.log("PAGING", "Starting Paging Search Scenario...")
        
        # 1. Create 5 documents with 'PagingTest' keyword
        page_keyword = "PagingTestXYZ"
        created_ids = []
        for i in range(5):
            payload = {"title": f"{page_keyword} Doc {i}", "content": f"Content {i}"}
            resp = self.session.post(f"{BASE_URL}/documents", json=payload, timeout=TIMEOUT)
            assert resp.status_code == 201
            created_ids.append(resp.json()["id"])
            
        try:
            # 2. Request page 0 with size 2
            resp = self.session.get(f"{BASE_URL}/documents/search", params={"query": page_keyword, "page": 0, "size": 2}, timeout=TIMEOUT)
            assert resp.status_code == 200
            data = resp.json()
            
            assert len(data["content"]) == 2
            assert data["totalElements"] == 5
            assert data["totalPages"] == 3
            assert data["number"] == 0 # Current page
            
            # 3. Request page 2 (the last page)
            resp_last = self.session.get(f"{BASE_URL}/documents/search", params={"query": page_keyword, "page": 2, "size": 2}, timeout=TIMEOUT)
            assert resp_last.status_code == 200
            data_last = resp_last.json()
            assert len(data_last["content"]) == 1 # Only 1 left (0,1; 2,3; 4)
            assert data_last["last"] is True
            
            self.log("PAGING", "Paging search scenario verified (Size, TotalElements, TotalPages).")
            
        finally:
            # Cleanup
            for did in created_ids:
                self.session.delete(f"{BASE_URL}/documents/{did}", timeout=TIMEOUT)

if __name__ == "__main__":
    verifier = ScenarioVerifier()
    verifier.run()
