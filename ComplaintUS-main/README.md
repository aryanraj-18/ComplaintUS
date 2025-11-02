# 🧾 ComplaintUS — Anonymous Complaint Management System  

> A modern, privacy-focused complaint management platform built with **Java Swing**, **MySQL**, and **FlatLaf** for a sleek, professional UI.

---

## 🚀 Overview  

**ComplaintUS** allows users to **submit anonymous complaints**, **track their status**, and enables admins to **review, update, or delete complaints** through a clean and secure dashboard.  
Built with a **modern FlatLaf dark interface**, it feels like a real-world desktop application.

---

## ✨ Features  

### 🧍 User Features  
- 📝 Submit complaints **anonymously** (no personal info required).  
- 🎯 Choose **category** (e.g., Academic, Infrastructure, Harassment).  
- ⚡ Assign **priority** (Low / Medium / High / Critical).  
- 🔍 Track complaint using a unique **Tracking ID**.  

### 👨‍💼 Admin Features  
- 🗂️ View all submitted complaints in a **sortable table view**.  
- 🔎 **View full complaint details** in a popup.  
- ✏️ **Update complaint status** via dropdown (Pending, In Progress, Resolved, Closed).  
- ❌ **Delete complaints** instantly with confirmation.  
- 🔁 Refresh and logout functionality.  

---

## 🖼️ Screenshots  

| Submit Complaint | Admin Dashboard |
|------------------|----------------|
| ![Submit Page](docs/submit.png) | ![Admin Page](docs/admin.png) |

*(Add your screenshots in a `docs/` folder in your repo)*  

---

## ⚙️ Tech Stack  

| Component | Technology |
|------------|-------------|
| Language | Java (JDK 8+) |
| GUI | Java Swing + FlatLaf (Dark Theme) |
| Database | MySQL |
| Connector | MySQL Connector/J |
| IDE (Optional) | IntelliJ IDEA / VS Code / NetBeans |

---

## 🧩 Database Setup  

Create a MySQL database named `complaint_db`, then run the following SQL command:

```sql
CREATE TABLE complaints (
    tracking_id VARCHAR(50) PRIMARY KEY,
    category VARCHAR(50),
    priority VARCHAR(20),
    complaint_text TEXT,
    status VARCHAR(20)
);
