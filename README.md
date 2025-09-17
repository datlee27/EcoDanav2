cố lên các bạn
# 🚀 Git Workflow cho Team EcoDana_v1.1

## 1️⃣ Clone repo lần đầu
Mỗi thành viên chỉ cần làm 1 lần:
```bash
git clone https://github.com/datlee27/EcoDana_v1.1.git
cd EcoDana_v1.1
```

---

Nếu bạn chỉ muốn <nhanh'> giống hệt main
```bash
git checkout <ten_nhanh>
git reset --hard main
git push origin <ten_nhanh> --force

```

---
## 2️⃣ Checkout sang nhánh của mình
👉 Mỗi người có **nhánh riêng** để code (do lead tạo sẵn).  
- Ky:
  ```bash
  git checkout ky
  ```
- Uyen:
  ```bash
  git checkout uyen
  ```
- Nguyen:
  ```bash
  git checkout nguyen
  ```

📌 Kiểm tra nhánh hiện tại:
```bash
git branch
```

---

## 3️⃣ Cập nhật code mới nhất từ GitHub (trước khi code)
```bash
git pull origin main
```

---

## 4️⃣ Quy trình làm việc hằng ngày
1. **Code / chỉnh sửa file**  
2. **Add & Commit**  
   ```bash
   git add .
   git commit -m "Mô tả ngắn gọn thay đổi"
   ```
3. **Push code lên nhánh của mình**  
   ```bash
   git push origin <tên_nhánh_của_mình>
   ```

Ví dụ của Ky:
```bash
git push origin ky
```

---

## 5️⃣ Merge code (chỉ lead làm)
- Lead sẽ tổng hợp code vào nhánh `dev`:  
  ```bash
  git checkout dev
  git pull origin dev
  git merge origin/ky     # hoặc uyen, nguyen
  git push origin dev
  ```

- Khi `dev` ổn định → merge vào `main`:  
  ```bash
  git checkout main
  git pull origin main
  git merge dev
  git push origin main
  ```

---

## ✅ Nguyên tắc
- **Không ai push trực tiếp vào `main` hoặc `dev`** → chỉ push vào nhánh cá nhân.  
- Luôn `git pull` trước khi code để tránh xung đột.  
- Commit ngắn gọn, rõ ràng.  
