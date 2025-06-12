# 📱 UOC Daily – Campus News Android App

Welcome to **UOC Daily** — a modern mobile application that keeps university students and staff updated with the latest academic, event, and sports news.  
Developed using **Android Studio (Java)** with **Firebase** backend support.

---

## 🚀 Features

### 👤 For Normal Users
- 🔐 Sign In / Sign Up
- 📰 View categorized news (Academic, Events, Sports)
- 🔍 Search news by title
- 🗂 Scrollable, card-based news display
- 📖 Click a card to read full article
- 👤 View and edit profile

### 🛠️ For Admin Users
- 🔑 Role-based login redirection
- 👥 Manage Users (view and delete)
- 📝 Manage News:
  - Add news with category, title, description, image
  - View and delete news by ID
- 🖼 Upload news images from local storage to Firebase
- 📦 All data stored in Firebase Realtime DB

---

## 🖼 Screens Overview

### 🟣 Splash Screen
- App logo with delay
- Auto-check login status

### 🔐 Auth Screens
- **Sign In**
- **Sign Up**
- Firebase Auth integration

### 📰 Main News Screen
- Home with all news
- Bottom navigation bar:
  - Home | Academic | Events | Sports | Developer Info

### 📚 Academic / Events / Sports Screens
- Filtered by category
- Back + search bar
- Scrollable card list

### 📖 Read News Screen
- Opens when a card is clicked
- Full article view with image, title, description

### ⚙️ Admin Dashboard
- Two cards:
  - 📋 Manage Users
  - 📰 Manage News

### 👥 Manage Users
- User list with:
  - Name, Email, Role
  - Search bar
  - Delete option

### 🗞️ Manage News
- Add news:
  - Category dropdown
  - Title, Description
  - Image upload
- List all news cards (with delete icon)

---

## 🛠️ Technologies Used

| Layer        | Tech                                 |
|--------------|--------------------------------------|
| 💻 Frontend  | Android Studio, Java, XML            |
| ☁️ Backend   | Firebase Auth, Firebase Realtime DB, Firebase Storage |
| 🖼 UI        | XD Design converted to custom layouts |
| 📦 Storage   | Firebase Storage (Free Tier)          |

---

## 🧠 Architecture

- 🔐 Role-based login redirection (admin/user)
- 🧩 Reusable UI components (card views)
- ☁️ Firebase-powered data sync and storage
- 🪝 Modular activity-per-screen structure

---

## 🏁 How to Run

1. Clone the repo:
   ```bash
   git clone https://github.com/sahansara/UocDaily.git

2.Open with Android Studio <br>
<br>
3.Add your own google-services.json to /app directory (from Firebase Console) <br>
<br>
4.Make sure Firebase rules allow read/write or authenticated access<br>
 <br>
5.Build and Run! 🚀<br>
 <br>
🧑‍💻 Developer Info<br>
 <br>
📧 Created by: Sahansara<br>
 <br>
📍 Technologies: Java + Firebase + XML<br>
 <br>
🔧 Contact me via GitHub Issues if any problem<br>

