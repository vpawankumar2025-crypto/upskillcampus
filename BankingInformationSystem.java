import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Banking Information System
 * Core Java Project - Upskill Campus Internship
 */
public class BankingInformationSystem {

    // ─── Data Models ───────────────────────────────────────────────────────────

    static class Transaction {
        String type;
        double amount;
        double balanceAfter;
        String date;
        String description;

        Transaction(String type, double amount, double balanceAfter, String description) {
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.description = description;
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
    }

    static class BankAccount {
        String accountNumber;
        String name;
        String address;
        String phone;
        String email;
        String password;
        double balance;
        List<Transaction> transactions = new ArrayList<>();

        BankAccount(String accountNumber, String name, String address, String phone,
                    String email, String password, double initialDeposit) {
            this.accountNumber = accountNumber;
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.password = password;
            this.balance = initialDeposit;
            if (initialDeposit > 0) {
                transactions.add(new Transaction("CREDIT", initialDeposit, initialDeposit, "Initial Deposit"));
            }
        }
    }

    // ─── State ─────────────────────────────────────────────────────────────────

    static Map<String, BankAccount> accounts = new HashMap<>();
    static BankAccount loggedInAccount = null;
    static Scanner scanner = new Scanner(System.in);
    static int accountCounter = 1001;

    // ─── Utilities ─────────────────────────────────────────────────────────────

    static void printHeader(String title) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.printf( "║  %-44s║%n", title);
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    static void printLine() {
        System.out.println("──────────────────────────────────────────────");
    }

    static String generateAccountNumber() {
        return "ACC" + (accountCounter++);
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double val = Double.parseDouble(scanner.nextLine().trim());
                if (val < 0) { System.out.println("  [!] Amount cannot be negative."); continue; }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid amount. Please enter a number.");
            }
        }
    }

    // ─── Registration ──────────────────────────────────────────────────────────

    static void registerUser() {
        printHeader("USER REGISTRATION");
        String name    = readLine("  Full Name      : ");
        String address = readLine("  Address        : ");
        String phone   = readLine("  Phone Number   : ");
        String email   = readLine("  Email Address  : ");

        String password, confirm;
        while (true) {
            password = readLine("  Password       : ");
            confirm  = readLine("  Confirm Pass   : ");
            if (password.equals(confirm) && password.length() >= 4) break;
            System.out.println("  [!] Passwords don't match or too short (min 4 chars).");
        }

        double initialDeposit = readDouble("  Initial Deposit: ₹");

        String accNo = generateAccountNumber();
        BankAccount account = new BankAccount(accNo, name, address, phone, email, password, initialDeposit);
        accounts.put(accNo, account);

        printLine();
        System.out.println("  ✔ Registration Successful!");
        System.out.println("  ► Your Account Number : " + accNo);
        System.out.printf( "  ► Opening Balance     : ₹%.2f%n", initialDeposit);
        System.out.println("  Please save your account number safely.");
        printLine();
    }

    // ─── Login ─────────────────────────────────────────────────────────────────

    static boolean login() {
        printHeader("LOGIN");
        String accNo    = readLine("  Account Number : ");
        String password = readLine("  Password       : ");

        BankAccount acc = accounts.get(accNo);
        if (acc != null && acc.password.equals(password)) {
            loggedInAccount = acc;
            System.out.println("\n  ✔ Welcome back, " + acc.name + "!");
            return true;
        }
        System.out.println("\n  [!] Invalid account number or password.");
        return false;
    }

    // ─── Deposit ───────────────────────────────────────────────────────────────

    static void deposit() {
        printHeader("DEPOSIT");
        double amount = readDouble("  Deposit Amount : ₹");
        if (amount <= 0) { System.out.println("  [!] Amount must be greater than 0."); return; }

        loggedInAccount.balance += amount;
        loggedInAccount.transactions.add(
            new Transaction("CREDIT", amount, loggedInAccount.balance, "Cash Deposit"));

        System.out.println("\n  ✔ Deposit Successful!");
        System.out.printf( "  ► Deposited    : ₹%.2f%n", amount);
        System.out.printf( "  ► New Balance  : ₹%.2f%n", loggedInAccount.balance);
    }

    // ─── Withdrawal ────────────────────────────────────────────────────────────

    static void withdraw() {
        printHeader("WITHDRAWAL");
        System.out.printf("  Available Balance : ₹%.2f%n", loggedInAccount.balance);
        double amount = readDouble("  Withdraw Amount : ₹");

        if (amount <= 0) { System.out.println("  [!] Amount must be greater than 0."); return; }
        if (amount > loggedInAccount.balance) {
            System.out.println("  [!] Insufficient funds. Transaction declined.");
            return;
        }

        loggedInAccount.balance -= amount;
        loggedInAccount.transactions.add(
            new Transaction("DEBIT", amount, loggedInAccount.balance, "Cash Withdrawal"));

        System.out.println("\n  ✔ Withdrawal Successful!");
        System.out.printf( "  ► Withdrawn    : ₹%.2f%n", amount);
        System.out.printf( "  ► New Balance  : ₹%.2f%n", loggedInAccount.balance);
    }

    // ─── Fund Transfer ─────────────────────────────────────────────────────────

    static void fundTransfer() {
        printHeader("FUND TRANSFER");
        System.out.printf("  Your Balance    : ₹%.2f%n", loggedInAccount.balance);
        String toAccNo = readLine("  Recipient Acc # : ");

        if (toAccNo.equals(loggedInAccount.accountNumber)) {
            System.out.println("  [!] Cannot transfer to your own account.");
            return;
        }

        BankAccount recipient = accounts.get(toAccNo);
        if (recipient == null) {
            System.out.println("  [!] Recipient account not found.");
            return;
        }

        double amount = readDouble("  Transfer Amount : ₹");
        if (amount <= 0) { System.out.println("  [!] Amount must be greater than 0."); return; }
        if (amount > loggedInAccount.balance) {
            System.out.println("  [!] Insufficient funds. Transfer declined.");
            return;
        }

        loggedInAccount.balance -= amount;
        recipient.balance       += amount;

        loggedInAccount.transactions.add(
            new Transaction("DEBIT", amount, loggedInAccount.balance,
                "Transfer to " + recipient.name + " (" + toAccNo + ")"));
        recipient.transactions.add(
            new Transaction("CREDIT", amount, recipient.balance,
                "Transfer from " + loggedInAccount.name + " (" + loggedInAccount.accountNumber + ")"));

        System.out.println("\n  ✔ Transfer Successful!");
        System.out.printf( "  ► Transferred  : ₹%.2f%n", amount);
        System.out.printf( "  ► To Account   : %s (%s)%n", recipient.name, toAccNo);
        System.out.printf( "  ► Your Balance : ₹%.2f%n", loggedInAccount.balance);
    }

    // ─── Account Statement ─────────────────────────────────────────────────────

    static void accountStatement() {
        printHeader("ACCOUNT STATEMENT");
        System.out.printf("  Account   : %s%n", loggedInAccount.accountNumber);
        System.out.printf("  Holder    : %s%n", loggedInAccount.name);
        System.out.printf("  Balance   : ₹%.2f%n", loggedInAccount.balance);
        printLine();
        System.out.printf("  %-20s %-8s %-12s %-12s%n", "Date/Time", "Type", "Amount(₹)", "Balance(₹)");
        printLine();

        if (loggedInAccount.transactions.isEmpty()) {
            System.out.println("  No transactions found.");
        } else {
            for (Transaction t : loggedInAccount.transactions) {
                System.out.printf("  %-20s %-8s %-12.2f %-12.2f%n",
                    t.date, t.type, t.amount, t.balanceAfter);
                System.out.printf("  Desc: %s%n", t.description);
                System.out.println();
            }
        }
        printLine();
    }

    // ─── View / Update Account ─────────────────────────────────────────────────

    static void viewAccount() {
        printHeader("ACCOUNT DETAILS");
        System.out.printf("  Account Number : %s%n", loggedInAccount.accountNumber);
        System.out.printf("  Name           : %s%n", loggedInAccount.name);
        System.out.printf("  Address        : %s%n", loggedInAccount.address);
        System.out.printf("  Phone          : %s%n", loggedInAccount.phone);
        System.out.printf("  Email          : %s%n", loggedInAccount.email);
        System.out.printf("  Balance        : ₹%.2f%n", loggedInAccount.balance);
    }

    static void updateAccount() {
        printHeader("UPDATE ACCOUNT");
        System.out.println("  Leave blank to keep existing value.\n");

        String newName = readLine("  New Name    [" + loggedInAccount.name + "] : ");
        String newAddr = readLine("  New Address [" + loggedInAccount.address + "] : ");
        String newPhone= readLine("  New Phone   [" + loggedInAccount.phone + "] : ");
        String newEmail= readLine("  New Email   [" + loggedInAccount.email + "] : ");

        if (!newName.isEmpty())  loggedInAccount.name    = newName;
        if (!newAddr.isEmpty())  loggedInAccount.address = newAddr;
        if (!newPhone.isEmpty()) loggedInAccount.phone   = newPhone;
        if (!newEmail.isEmpty()) loggedInAccount.email   = newEmail;

        System.out.println("\n  ✔ Account information updated successfully!");
    }

    // ─── Dashboard ─────────────────────────────────────────────────────────────

    static void dashboard() {
        while (true) {
            printHeader("BANKING DASHBOARD — " + loggedInAccount.name);
            System.out.printf("  Account: %s  |  Balance: ₹%.2f%n%n",
                loggedInAccount.accountNumber, loggedInAccount.balance);
            System.out.println("  [1] View Account Details");
            System.out.println("  [2] Update Account Info");
            System.out.println("  [3] Deposit");
            System.out.println("  [4] Withdraw");
            System.out.println("  [5] Fund Transfer");
            System.out.println("  [6] Account Statement");
            System.out.println("  [7] Logout");
            printLine();

            String choice = readLine("  Enter choice: ");
            switch (choice) {
                case "1" -> viewAccount();
                case "2" -> updateAccount();
                case "3" -> deposit();
                case "4" -> withdraw();
                case "5" -> fundTransfer();
                case "6" -> accountStatement();
                case "7" -> { loggedInAccount = null; System.out.println("\n  Logged out successfully."); return; }
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ─── Main Menu ─────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      BANKING INFORMATION SYSTEM              ║");
        System.out.println("║      Upskill Campus — Core Java Project      ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        while (true) {
            printHeader("MAIN MENU");
            System.out.println("  [1] Register New Account");
            System.out.println("  [2] Login");
            System.out.println("  [3] Exit");
            printLine();

            String choice = readLine("  Enter choice: ");
            switch (choice) {
                case "1" -> registerUser();
                case "2" -> { if (login()) dashboard(); }
                case "3" -> {
                    System.out.println("\n  Thank you for using Banking Information System.");
                    System.out.println("  Goodbye!\n");
                    System.exit(0);
                }
                default -> System.out.println("  [!] Invalid option. Please try again.");
            }
        }
    }
}
