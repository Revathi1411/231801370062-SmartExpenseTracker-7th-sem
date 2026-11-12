SMART EXPENSE TRACKER - SETUP (5 steps)

1. Firebase: https://console.firebase.google.com -> create project -> Add Android app
   Package name: com.example.expensetracker
   Download google-services.json and put it in:  app/google-services.json
2. Firebase console -> Build -> Authentication -> Sign-in method -> enable Email/Password.
3. Open this folder in Android Studio (Hedgehog or newer, JDK 17). Let Gradle sync.
   If it asks for a Gradle version, use 8.7 or newer.
4. Run on an emulator/phone with Google Play services and internet.
5. To test receipt scanning, put a receipt photo in the emulator gallery.

Files (app/src/main/java/com/example/expensetracker):
  LoginActivity, RegisterActivity  - Firebase login / registration
  MainActivity                     - dashboard, expense list, month total, budget alert, logout
  AddExpenseActivity               - add AND edit expense (edit mode when "expense_id" is passed)
  SummaryActivity                  - category-wise totals with progress bars
  BudgetActivity                   - set / view monthly budget
  DatabaseHelper                   - SQLite (expenses) + budget in SharedPreferences
  Expense, ExpenseAdapter          - data class and RecyclerView adapter
  ReceiptScanner                   - ML Kit text recognition + amount finder
  CurrencyConverter                - Frankfurter API call (HttpURLConnection)
