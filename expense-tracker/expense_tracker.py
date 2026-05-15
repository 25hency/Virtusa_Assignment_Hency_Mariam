import json
import os
from collections import defaultdict
from datetime import datetime

import matplotlib.pyplot as plt


class ExpenseTracker:
    def __init__(self, data_file="expenses.json"):
        self.data_file = data_file
        self.categories = ["Food", "Travel", "Bills", "Entertainment", "Shopping", "Healthcare", "Other"]
        self.expenses = []
        self.load_data()

    def load_data(self):
        if os.path.exists(self.data_file):
            try:
                with open(self.data_file, "r", encoding="utf-8") as f:
                    self.expenses = json.load(f)
            except (json.JSONDecodeError, OSError):
                self.expenses = []

    def save_data(self):
        with open(self.data_file, "w", encoding="utf-8") as f:
            json.dump(self.expenses, f, indent=2)

    def add_expense(self, date, category, amount, description):
        if category not in self.categories:
            return False
        try:
            datetime.strptime(date, "%Y-%m-%d")
            amount = float(amount)
            if amount <= 0:
                return False
        except ValueError:
            return False

        self.expenses.append(
            {
                "date": date,
                "category": category,
                "amount": amount,
                "description": description,
            }
        )
        self.save_data()
        return True

    def monthly_expenses(self, year, month):
        out = []
        for e in self.expenses:
            d = datetime.strptime(e["date"], "%Y-%m-%d")
            if d.year == year and d.month == month:
                out.append(e)
        return out

    def monthly_breakdown(self, year, month):
        breakdown = defaultdict(float)
        for e in self.monthly_expenses(year, month):
            breakdown[e["category"]] += e["amount"]
        return dict(breakdown)

    def monthly_summary(self, year, month):
        items = self.monthly_expenses(year, month)
        breakdown = self.monthly_breakdown(year, month)
        total = sum(x["amount"] for x in items)
        if not breakdown:
            return items, breakdown, total, None, 0
        top_cat = max(breakdown, key=breakdown.get)
        return items, breakdown, total, top_cat, breakdown[top_cat]

    def show_charts(self, year, month):
        _, breakdown, total, _, _ = self.monthly_summary(year, month)
        if not breakdown:
            print("No data to plot for this month.")
            return

        labels = list(breakdown.keys())
        values = list(breakdown.values())

        plt.figure(figsize=(12, 5))

        plt.subplot(1, 2, 1)
        plt.pie(values, labels=labels, autopct="%1.1f%%", startangle=90)
        plt.title(f"Category Share {year}-{month:02d}")

        plt.subplot(1, 2, 2)
        plt.bar(labels, values)
        plt.title(f"Category Spend {year}-{month:02d}")
        plt.ylabel("Amount")
        plt.xticks(rotation=25, ha="right")

        plt.suptitle(f"Monthly Total: Rs. {total:.2f}")
        plt.tight_layout()
        plt.show()

    def export_to_json(self, filename):
        try:
            with open(filename, 'w', encoding='utf-8') as jsonfile:
                json.dump(self.expenses, jsonfile, indent=2)
            print(f"✓ Expenses exported to JSON: {filename}")
            return True
        except IOError as e:
            print(f"✗ Error exporting to JSON: {e}")
            return False


class ExpenseTrackerCLI:
    def __init__(self):
        self.app = ExpenseTracker()

    def add_expense(self):
        print("\nAdd Expense")
        date = input("Date (YYYY-MM-DD): ").strip()
        print("Categories:", ", ".join(self.app.categories))
        category = input("Category: ").strip()
        amount = input("Amount: ").strip()
        description = input("Description: ").strip()

        if self.app.add_expense(date, category, amount, description):
            print("Saved")
        else:
            print("Invalid input")

    def monthly_insights(self):
        print("\nMonthly Insights")
        now = datetime.now()
        year = int(input(f"Year [{now.year}]: ") or now.year)
        month = int(input(f"Month [{now.month}]: ") or now.month)

        items, breakdown, total, top_cat, top_amt = self.app.monthly_summary(year, month)
        if not items:
            print("No expenses for this month")
            return

        print(f"\nSummary {year}-{month:02d}")
        print(f"Total: Rs. {total:.2f}")
        print(f"Entries: {len(items)}")
        print(f"Highest Category: {top_cat} (Rs. {top_amt:.2f})")

        print("\nCategory Breakdown")
        for cat, amt in sorted(breakdown.items(), key=lambda x: x[1], reverse=True):
            print(f"{cat}: Rs. {amt:.2f}")

        
        self.app.show_charts(year, month)

    def view_expenses(self):
        print("\nView Expenses")
        now = datetime.now()
        year = int(input(f"Year [{now.year}]: ") or now.year)
        
        items = [e for e in self.app.expenses if datetime.strptime(e["date"], "%Y-%m-%d").year == year]
        if not items:
            print("No expenses found for this year")
            return
        
        print(f"\nAll Expenses for {year}")
        print(f"{'Date':<12} {'Category':<15} {'Amount':<10} {'Description':<20}")
        print("-" * 60)
        for item in items:
            print(f"{item['date']:<12} {item['category']:<15} Rs. {item['amount']:<8.2f} {item['description']:<20}")
        
        total = sum(item['amount'] for item in items)
        print("-" * 60)
        print(f"{'Annual Total':<12} {'':15} Rs. {total:<8.2f}")

    def view_category_breakdown(self):
        print("\nCategory Breakdown")
        now = datetime.now()
        year = int(input(f"Year [{now.year}]: ") or now.year)
        
        breakdown = defaultdict(float)
        for e in self.app.expenses:
            d = datetime.strptime(e["date"], "%Y-%m-%d")
            if d.year == year:
                breakdown[e["category"]] += e["amount"]
        
        total = sum(breakdown.values()) if breakdown else 0
        
        if not breakdown:
            print("No expenses for this year")
            return
        
        print(f"\nCategory Breakdown for {year}")
        print(f"{'Category':<15} {'Amount':<12} {'Percentage':<12}")
        print("-" * 40)
        for cat, amt in sorted(breakdown.items(), key=lambda x: x[1], reverse=True):
            percentage = (amt / total * 100) if total > 0 else 0
            print(f"{cat:<15} Rs. {amt:<10.2f} {percentage:.1f}%")
        print("-" * 40)
        print(f"{'Total':<15} Rs. {total:<10.2f}")

    def run(self):
        while True:
            print("\nSmart Expense Tracker")
            print("1. Add Expense")
            print("2. Monthly Summary")
            print("3. View Expenses")
            print("4. Category Breakdown")
            print("5. Export Data")
            print("6. Exit")
            choice = input("Choose (1-6): ").strip()

            if choice == "1":
                self.add_expense()
            elif choice == "2":
                self.monthly_insights()
            elif choice == "3":
                self.view_expenses()
            elif choice == "4":
                self.view_category_breakdown()
            elif choice == "5":
                self.export_import_menu()
            elif choice == "6":
                print("Bye")
                break
            else:
                print("Invalid choice")

    def export_import_menu(self):
        print("\nExport Data")
        filename = input("Enter JSON filename [expenses_export.json]: ").strip() or "expenses_export.json"
        self.app.export_to_json(filename)


if __name__ == "__main__":
    ExpenseTrackerCLI().run()
