import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Book {
    private int bookId;
    private String title;
    private String author;
    private int totalCopies;
    private int availableCopies;
    private String category;
    private double price;

    public Book(int bookId, String title, String author, int totalCopies, String category, double price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.category = category;
        this.price = price;
    }

    public int getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPrice(double price) { this.price = price; }

    public boolean issueBook() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    @Override
    public String toString() {
        return "ID=" + bookId + ", Title=" + title + ", Author=" + author + ", Available=" + availableCopies + "/" + totalCopies;
    }
}

class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private List<IssuedBook> issuedBooks;

    public User(int userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registrationDate = LocalDate.now();
        this.issuedBooks = new ArrayList<>();
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public List<IssuedBook> getIssuedBooks() { return issuedBooks; }

    public void issueBook(Book book, int days) {
        if (book.issueBook()) {
            issuedBooks.add(new IssuedBook(book, LocalDate.now(), days));
        }
    }

    public boolean returnBook(Book book) {
        for (IssuedBook issuedBook : issuedBooks) {
            if (issuedBook.getBook().getBookId() == book.getBookId() && !issuedBook.isReturned()) {
                issuedBook.setReturned(true);
                issuedBook.setReturnDate(LocalDate.now());
                book.returnBook();
                return true;
            }
        }
        return false;
    }

    public double calculateFine() {
        double totalFine = 0;
        for (IssuedBook issuedBook : issuedBooks) {
            totalFine += issuedBook.calculateFine();
        }
        return totalFine;
    }
}

class IssuedBook {
    private Book book;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean returned;
    private static final double FINE_PER_DAY = 5.0;

    public IssuedBook(Book book, LocalDate issueDate, int days) {
        this.book = book;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(days);
        this.returned = false;
    }

    public Book getBook() { return book; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return returned; }

    public void setReturned(boolean returned) { this.returned = returned; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isOverdue() {
        if (returned) {
            return returnDate.isAfter(dueDate);
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        LocalDate checkDate = returned ? returnDate : LocalDate.now();
        if (checkDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, checkDate);
        }
        return 0;
    }

    public double calculateFine() {
        return isOverdue() ? getDaysOverdue() * FINE_PER_DAY : 0;
    }
}

class Library {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private int nextBookId = 1;
    private int nextUserId = 1;

    public Library() {
        addBook("The Silent Patient", "Alex Michaelides", 5, "Mystery", 399.99);
        addBook("Atomic Habits", "James Clear", 8, "Self-Help", 599.99);
    }

    public void addBook(String title, String author, int copies, String category, double price) {
        books.add(new Book(nextBookId++, title, author, copies, category, price));
    }

    public void addUser(String name, String email, String phone) {
        users.add(new User(nextUserId++, name, email, phone));
    }

    public Book searchBookByTitle(String title) {
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                return book;
            }
        }
        return null;
    }

    public Book searchBookByAuthor(String author) {
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                return book;
            }
        }
        return null;
    }

    public Book getBookById(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                return book;
            }
        }
        return null;
    }

    public User getUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    public List<Book> getAllBooks() { return new ArrayList<>(books); }
    public List<User> getAllUsers() { return new ArrayList<>(users); }

    public void updateBook(int bookId, String title, String author, double price) {
        Book book = getBookById(bookId);
        if (book != null) {
            book.setTitle(title);
            book.setAuthor(author);
            book.setPrice(price);
        }
    }

    public void removeBook(int bookId) {
        books.removeIf(book -> book.getBookId() == bookId);
    }

    public boolean issueBook(int userId, int bookId, int days) {
        User user = getUserById(userId);
        Book book = getBookById(bookId);
        if (user != null && book != null && book.getAvailableCopies() > 0) {
            user.issueBook(book, days);
            return true;
        }
        return false;
    }

    public boolean returnBook(int userId, int bookId) {
        User user = getUserById(userId);
        Book book = getBookById(bookId);
        if (user != null && book != null) {
            return user.returnBook(book);
        }
        return false;
    }

    public double getUserFine(int userId) {
        User user = getUserById(userId);
        return user != null ? user.calculateFine() : 0;
    }
}

public class LibraryManagement {
    public static void main(String[] args) {
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Update Book");
            System.out.println("4. Register User");
            System.out.println("5. Issue Book");
            System.out.println("6. Return Book");
            System.out.println("7. Search by Title");
            System.out.println("8. Search by Author");
            System.out.println("9. View Fine");
            System.out.println("10. List Books");
            System.out.println("11. List Users");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Author: ");
                    String author = scanner.nextLine();
                    System.out.print("Copies: ");
                    int copies = Integer.parseInt(scanner.nextLine());
                    System.out.print("Category: ");
                    String category = scanner.nextLine();
                    System.out.print("Price: ");
                    double price = Double.parseDouble(scanner.nextLine());
                    library.addBook(title, author, copies, category, price);
                    System.out.println("Book added");
                    break;
                case 2:
                    System.out.print("Book ID: ");
                    library.removeBook(Integer.parseInt(scanner.nextLine()));
                    System.out.println("Book removed");
                    break;
                case 3:
                    System.out.print("Book ID: ");
                    int bookId = Integer.parseInt(scanner.nextLine());
                    System.out.print("New Title: ");
                    String newTitle = scanner.nextLine();
                    System.out.print("New Author: ");
                    String newAuthor = scanner.nextLine();
                    System.out.print("New Price: ");
                    double newPrice = Double.parseDouble(scanner.nextLine());
                    library.updateBook(bookId, newTitle, newAuthor, newPrice);
                    System.out.println("Book updated");
                    break;
                case 4:
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Phone: ");
                    String phone = scanner.nextLine();
                    library.addUser(name, email, phone);
                    System.out.println("User registered");
                    break;
                case 5:
                    System.out.print("User ID: ");
                    int issueUserId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Book ID: ");
                    int issueBookId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Days: ");
                    int days = Integer.parseInt(scanner.nextLine());
                    System.out.println(library.issueBook(issueUserId, issueBookId, days) ? "Book issued" : "Cannot issue book");
                    break;
                case 6:
                    System.out.print("User ID: ");
                    int returnUserId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Book ID: ");
                    int returnBookId = Integer.parseInt(scanner.nextLine());
                    System.out.println(library.returnBook(returnUserId, returnBookId) ? "Book returned" : "Cannot return book");
                    break;
                case 7:
                    System.out.print("Title: ");
                    Book titleBook = library.searchBookByTitle(scanner.nextLine());
                    System.out.println(titleBook != null ? titleBook : "Book not found");
                    break;
                case 8:
                    System.out.print("Author: ");
                    Book authorBook = library.searchBookByAuthor(scanner.nextLine());
                    System.out.println(authorBook != null ? authorBook : "Book not found");
                    break;
                case 9:
                    System.out.print("User ID: ");
                    int fineUserId = Integer.parseInt(scanner.nextLine());
                    System.out.println("Fine: Rs. " + String.format("%.2f", library.getUserFine(fineUserId)));
                    break;
                case 10:
                    for (Book book : library.getAllBooks()) {
                        System.out.println(book);
                    }
                    break;
                case 11:
                    for (User user : library.getAllUsers()) {
                        System.out.println("ID=" + user.getUserId() + ", Name=" + user.getName() + ", Email=" + user.getEmail() + ", Phone=" + user.getPhone());
                    }
                    break;
                case 0:
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}