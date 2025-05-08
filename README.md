## ExpenseIt

ExpenseIt is a personal expense tracking and receipt management Android application. It enables you to log expenses, scan receipts, organize spending by categories, and visualize statistics with interactive charts.

## Features

- Manual Expense Entry: Quickly add, edit, and delete expenses.
- Receipt Scanning: Capture receipts with OCR, extract line items, and create expense records.
- Categories: Organize expenses by customizable categories.
- Statistics Dashboard: View spending breakdowns via pie and line charts, swipe by month.
- Currency Support: Choose from a wide range of ISO currencies.

## Tech Stack
- Language: Kotlin
- Architecture: MVVM + Repository Pattern
- DI: Hilt
- Persistence: Room (SQLite)
- UI: Jetpack Compose + MPAndroidChart
- OCR: Azure AI Document Scanner (receipt scanning)
- Storage: Firebase Storage, Room DB

## Installation

1. Clone the repository: Github Repo: https://github.com/marctaguz/ExpenseIt

2. Open the project in Android Studio or a suitable IDE.

3. In Constants.kt, replace the placeholder API_KEY and ENDPOINT with your own values for the receipt scanning service.

4. Build and run: Allow Gradle sync, then click Run on an emulator or device