import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.*;


class Patient 
{
    int id;
    String name;
    int severity;
    LocalDateTime arrivalTime;

    Patient(int id, String name, int severity) 
    {
        this.id = id;
        this.name = name;
        this.severity = Math.max(1, Math.min(severity, 10));
        this.arrivalTime = LocalDateTime.now();
    }

    public String toString() 
    {
        return String.format("[ID:%d, %s, Severity:%d, Arrived:%s]",
                id, name, severity, arrivalTime);
    }

    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient other = (Patient) o;
        return this.id == other.id;
    }

    public int hashCode() {
        return Objects.hash(id);
    }
}


class Node 
{
    Patient patient;
    Node prev, next;

    Node(Patient patient) {
        this.patient = patient;
    }
}


class HospitalER 
{
    private Node head, tail;
    private HashMap<Integer, Node> patientMap = new HashMap<>();
    private PriorityQueue<Patient> pq;
    private int idCounter = 1;


    private static final String PATIENT_FILE = "patients.txt";
    private static final String LOG_FILE = "treated_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    HospitalER() 
    {
        pq = new PriorityQueue<>((a, b) -> {
            if (b.severity != a.severity) return b.severity - a.severity;
            return a.arrivalTime.compareTo(b.arrivalTime);
        });
    }

    public void loadFromFile() 
    {
        File file = new File(PATIENT_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) 
        {
            String line;
            while ((line = br.readLine()) != null) 
            {
                String[] parts = line.split(",", 4);
                if (parts.length < 4) continue;
                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int severity = Integer.parseInt(parts[2].trim());
                LocalDateTime time = LocalDateTime.parse(parts[3].trim(), FORMATTER);

                Patient p = new Patient(id, name, severity);
                p.arrivalTime = time;
                Node node = new Node(p);
                addToDoublyLinkedList(node);
                patientMap.put(p.id, node);
                pq.offer(p);
                idCounter = Math.max(idCounter, id + 1);
            }
            System.out.println("Loaded existing patients from file.");
        } 
        catch (Exception e) 
        {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    public void saveToFile() 
    {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PATIENT_FILE))) {
            for (Node node = head; node != null; node = node.next) {
                Patient p = node.patient;
                pw.println(p.id + "," + p.name + "," + p.severity + "," + p.arrivalTime.format(FORMATTER));
            }
        } catch (Exception e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    private void logAction(String action, Patient p) 
    {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) 
        {
            pw.println(action + ": " + p.toString());
        } 
        catch (Exception e) 
        {
            System.out.println("Error writing log: " + e.getMessage());
        }
    }


    public void addPatient(String name, int severity) 
    {
        severity = clampSeverity(severity);
        Patient p = new Patient(idCounter++, name, severity);
        Node node = new Node(p);
        addToDoublyLinkedList(node);
        patientMap.put(p.id, node);
        pq.offer(p);
        saveToFile();
        System.out.println("Added: " + p);
    }

    public void addEmergencyPatient(String name, int severity) 
    {
        if (severity < 10) severity = 10;
        addPatient(name, severity);
    }

    public void updateSeverity(int id, int newSeverity) 
    {
        Node node = patientMap.get(id);
        if (node == null) {
            System.out.println("Patient not found.");
            return;
        }
        pq.remove(node.patient);
        node.patient.severity = clampSeverity(newSeverity);
        pq.offer(node.patient);
        saveToFile();
        System.out.println("Severity updated: " + node.patient);
    }

    public void removePatient(int id) 
    {
        Node node = patientMap.get(id);
        if (node == null) {
            System.out.println("Patient not found.");
            return;
        }
        pq.remove(node.patient);
        removeFromDoublyLinkedList(node);
        patientMap.remove(id);
        logAction("Removed", node.patient);
        saveToFile();
        System.out.println("Removed: " + node.patient);
    }

    public void treatNextPatient() 
    {
        if (pq.isEmpty()) {
            System.out.println("No patients to treat.");
            return;
        }
        Patient p = pq.poll();
        Node node = patientMap.get(p.id);
        if (node != null) {
            removeFromDoublyLinkedList(node);
            patientMap.remove(p.id);
        }
        logAction("Treated", p);
        saveToFile();
        System.out.println("Treating: " + p);
    }


    public void viewWaitingList()
    {
        if (head == null) {
            System.out.println("Waiting list is empty.");
            return;
        }
        System.out.println("Waiting List (Arrival Order):");
        for (Node t = head; t != null; t = t.next) {
            System.out.println("   " + t.patient);
        }
    }

    public void viewSeverityOrder() 
    {
        if (pq.isEmpty()) 
        {
            System.out.println("No patients in queue.");
            return;
        }
        System.out.println("Patients by Severity Order:");
        PriorityQueue<Patient> tempPQ = new PriorityQueue<>(pq);
        while (!tempPQ.isEmpty()) {
            System.out.println("   " + tempPQ.poll());
        }
    }

    public void searchPatientByName(String name) 
    {
        boolean found = false;
        for (Node node = head; node != null; node = node.next) 
        {
            if (node.patient.name.equalsIgnoreCase(name)) {
                System.out.println("Found: " + node.patient);
                found = true;
            }
        }
        if (!found) System.out.println("No patient found with name: " + name);
    }

    public void searchPatientById(int id) 
    {
        Node node = patientMap.get(id);
        if (node != null) System.out.println("Found by ID: " + node.patient);
        else System.out.println("⚠ No patient found with ID: " + id);
    }

    public void viewNextToTreat() 
    {
        if (pq.isEmpty()) System.out.println("⚠ No patients in queue.");
        else System.out.println("➡ Next to treat: " + pq.peek());
    }



    // ===================== UTILITIES =====================
    public boolean isEmpty() 
    { 
        return pq.isEmpty(); 
    }
    public int size() 
    { 
        return patientMap.size(); 
    }
    public List<Integer> getAllPatientIds() 
    { 
        return new ArrayList<>(patientMap.keySet()); 
    }

    private int clampSeverity(int s) 
    { 
        return Math.max(1, Math.min(s, 10)); 
    }

    private void addToDoublyLinkedList(Node node) 
    {
        if (head == null) 
        {
            head = tail = node;
        }
        else 
        {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }

    private void removeFromDoublyLinkedList(Node node) 
    {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;
        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;
        node.prev = node.next = null;
    }
}

// ===================== MAIN CLASS =====================
public class HR
{
    private static final String[] SAMPLE_NAMES = {
        "Aarav", "Isha", "Rohan", "Simran", "Kabir", "Ananya", "Dev", "Meera", "Raj", "Priya",
        "Neha", "Arjun", "Ira", "Vihaan", "Zara", "Vivaan", "Riya", "Kunal", "Tara", "Aditya"
    };
    private static final Random RAND = new Random();

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        HospitalER er = new HospitalER();
        er.loadFromFile();

        System.out.println("=== Hospital Emergency Room ===");
        System.out.println("1. Manual Mode");
        System.out.println("2. Auto-Simulation Mode");
        System.out.print("Choose: ");
        int mode = safeNextInt(sc, 1);

        if (mode == 1) manualMode(sc, er);
        else autoSimulation(er, 20, 800);
    }

    private static void manualMode(Scanner sc, HospitalER er) 
    {
        while (true) 
        {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Add Patient");
            System.out.println("2. Emergency Add Patient");
            System.out.println("3. Update Patient Severity");
            System.out.println("4. Remove Patient");
            System.out.println("5. Treat Next Patient");
            System.out.println("6. View Waiting List (Arrival Order)");
            System.out.println("7. View Patients by Severity Order");
            System.out.println("8. Search Patient by Name");
            System.out.println("9. Search Patient by ID");
            System.out.println("10. View Next Patient to Treat");
            System.out.println("11. Exit");
            System.out.print("Choose: ");

            int choice = safeNextInt(sc, 1);
            sc.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter severity (1-10): ");
                    int severity = safeNextInt(sc, 1);
                    er.addPatient(name, severity);
                }
                case 2 -> {
                    System.out.print("Enter name: ");
                    String eName = sc.nextLine();
                    System.out.print("Enter severity (1-10): ");
                    int eSeverity = safeNextInt(sc, 1);
                    er.addEmergencyPatient(eName, eSeverity);
                }
                case 3 -> {
                    System.out.print("Enter patient ID: ");
                    int editId = safeNextInt(sc, 1);
                    System.out.print("Enter new severity (1-10): ");
                    int newSeverity = safeNextInt(sc, 1);
                    er.updateSeverity(editId, newSeverity);
                }
                case 4 -> {
                    System.out.print("Enter patient ID: ");
                    int removeId = safeNextInt(sc, 1);
                    er.removePatient(removeId);
                }
                case 5 -> er.treatNextPatient();
                case 6 -> er.viewWaitingList();
                case 7 -> er.viewSeverityOrder();
                case 8 -> {
                    System.out.print("Enter patient name: ");
                    String searchName = sc.nextLine();
                    er.searchPatientByName(searchName);
                }
                case 9 -> {
                    System.out.print("Enter patient ID: ");
                    int searchId = safeNextInt(sc, 1);
                    er.searchPatientById(searchId);
                }
                case 10 -> er.viewNextToTreat();
                case 11 -> {
                    System.out.println("Exiting system...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void autoSimulation(HospitalER er, int steps, int delayMs) throws InterruptedException 
    {
        System.out.println("Auto-Simulation started (" + steps + " steps)...");
        for (int i = 1; i <= steps; i++) {
            int action = RAND.nextInt(5);
            if (er.isEmpty() && (action == 3 || action == 4)) action = RAND.nextInt(3);

            switch (action) {
                case 0 -> er.addPatient(randomName(), RAND.nextInt(10) + 1);
                case 1 -> er.addEmergencyPatient(randomName(), RAND.nextInt(10) + 1);
                case 2 -> {
                    List<Integer> ids = er.getAllPatientIds();
                    if (!ids.isEmpty()) {
                        int id = ids.get(RAND.nextInt(ids.size()));
                        er.updateSeverity(id, RAND.nextInt(10) + 1);
                    }
                }
                case 3 -> er.treatNextPatient();
                case 4 -> {
                    List<Integer> ids2 = er.getAllPatientIds();
                    if (!ids2.isEmpty()) {
                        int id = ids2.get(RAND.nextInt(ids2.size()));
                        er.removePatient(id);
                    }
                }
            }

            System.out.println("\n--- STATE after step " + i + " (size=" + er.size() + ") ---");
            er.viewWaitingList();
            er.viewNextToTreat();
            System.out.println("-----------------------------------------------------\n");

            Thread.sleep(delayMs);
        }
        System.out.println("Auto-Simulation finished.");
    }

    private static String randomName() { return SAMPLE_NAMES[RAND.nextInt(SAMPLE_NAMES.length)]; }

    private static int safeNextInt(Scanner sc, int defaultVal) 
    {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid integer: ");
            sc.next();
        }
        return sc.nextInt();
    }
}
