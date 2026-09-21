# Pharmacy-Inventory-Management-System
A desktop application for a pharmacy to manage its  inventory, process sales, and generate business report

## DEFAULT LOGIN CREDENTIAL
- Administrator : username = admin     password = admin123
- Cashier       : username = cashier   password = cash123

## SETUP INSTRUCTIONS

1. Install MySQL and start the MySQL service.
2. Run database/database.sql in MySQL Workbench (or `mysql -u root -p < database.sql`)
   to create the pims_db database, tables, and sample data.
3. Open the project in Apache NetBeans (File > Open Project).
4. Add the MySQL Connector/J .jar to the project's Libraries
   (right-click Libraries > Add Library, or Add JAR/Folder).
5. Open src/com/healthfirst/pims/db/DBConnection.java and confirm the
   DB_URL / DB_USER / DB_PASS values match your local MySQL setup.
6. Run the project (Main.java is the entry point).

## BUILDING A STANDALONE .EXE

Use Clean & Build in NetBeans to produce dist/PIMS.jar, then wrap it into an
.exe with a tool such as Launch4j (bundle the MySQL Connector/J jar with it).

## APPLICATION OVERVIEW

- Login screen redirects to the Admin Dashboard or Cashier Dashboard based on role.
- Admin Dashboard: Manage Medicines, Manage Suppliers, Manage Users, Reports.
- Cashier Dashboard: Point of Sale (cart + checkout + bill), Stock Check (read-only).
- Reports: Sales Report, Item-Wise Sales Report, Low Stock Report, Expiry Report.
