import java.util.*;
import java.time.*;

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

    public int hashCode() 
    {
        return Objects.hash(id);
    }
}

class Node 
{
    Patient patient;
    Node prev, next;

    Node(Patient patient) 
    {
        this.patient = patient;
    }
}


class HospitalER 
{
    private Node head, tail;
    private HashMap<Integer, Node> patientMap = new HashMap<>();
    private PriorityQueue<Patient> pq;
    private int idCounter = 1;

    HospitalER() 
    {
        pq = new PriorityQueue<>((a, b) -> {
            if (b.severity != a.severity) return b.severity - a.severity;
            return a.arrivalTime.compareTo(b.arrivalTime);
        });
    }

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

    
    public void addPatient(String name, int severity) 
    {
        severity = clampSeverity(severity);
        Patient p = new Patient(idCounter++, name, severity);
        Node node = new Node(p);
        addToDoublyLinkedList(node);
        patientMap.put(p.id, node);
        pq.offer(p);
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
        if (pq.isEmpty()) {
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
        for (Node node = head; node != null; node = node.next) {
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
        else System.out.println("No patient found with ID: " + id);
    }

    public void viewNextToTreat() 
    {
        if (pq.isEmpty()) System.out.println("No patients in queue.");
        else System.out.println("➡ Next to treat: " + pq.peek());
    }

}

public class HospitalEmergencyRoom 
{
    private static final String[] SAMPLE_NAMES = {
        "Aarav", "Isha", "Rohan", "Simran", "Kabir", "Ananya", "Dev", "Meera", "Raj", "Priya",
        "Neha", "Arjun", "Ira", "Vihaan", "Zara", "Vivaan", "Riya", "Kunal", "Tara", "Aditya"
    };
    private static final Random RAND = new Random();

    public static void main(String[] args) throws InterruptedException 
    {
        Scanner sc = new Scanner(System.in);
        HospitalER er = new HospitalER();

        System.out.println("=== Hospital Emergency Room ===");
        System.out.println("1. Manual Mode");
        System.out.println("2. Auto-Simulation Mode");
        System.out.print("Choose: ");
        int mode = safeNextInt(sc, 1);

        if (mode == 1) {
            manualMode(sc, er);
        } else {
            autoSimulation(er, 20, 800);
        }
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

            switch (choice) 
            {
                case 1:
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter severity (1-10): ");
                    int severity = safeNextInt(sc, 1);
                    er.addPatient(name, severity);
                    break;
                case 2:
                    System.out.print("Enter name: ");
                    String eName = sc.nextLine();
                    System.out.print("Enter severity (1-10): ");
                    int eSeverity = safeNextInt(sc, 1);
                    er.addEmergencyPatient(eName, eSeverity);
                    break;
                case 3:
                    System.out.print("Enter patient ID: ");
                    int editId = safeNextInt(sc, 1);
                    System.out.print("Enter new severity (1-10): ");
                    int newSeverity = safeNextInt(sc, 1);
                    er.updateSeverity(editId, newSeverity);
                    break;
                case 4:
                    System.out.print("Enter patient ID: ");
                    int removeId = safeNextInt(sc, 1);
                    er.removePatient(removeId);
                    break;
                case 5:
                    er.treatNextPatient();
                    break;
                case 6:
                    er.viewWaitingList();
                    break;
                case 7:
                    er.viewSeverityOrder();
                    break;
                case 8:
                    System.out.print("Enter patient name: ");
                    String searchName = sc.nextLine();
                    er.searchPatientByName(searchName);
                    break;
                case 9:
                    System.out.print("Enter patient ID: ");
                    int searchId = safeNextInt(sc, 1);
                    er.searchPatientById(searchId);
                    break;
                case 10:
                    er.viewNextToTreat();
                    break;
                case 11:
                    System.out.println("Exiting system...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void autoSimulation(HospitalER er, int steps, int delayMs) throws InterruptedException 
    {
        System.out.println("Auto-Simulation started (" + steps + " steps)...");
        for (int i = 1; i <= steps; i++) 
        {
            int action = RAND.nextInt(5);
            if (er.isEmpty() && (action == 3 || action == 4)) {
                action = RAND.nextInt(3);
            }

            switch (action) {
                case 0:
                    er.addPatient(randomName(), RAND.nextInt(10) + 1);
                    break;
                case 1:
                    er.addEmergencyPatient(randomName(), RAND.nextInt(10) + 1);
                    break;
                case 2:
                    List<Integer> ids = er.getAllPatientIds();
                    if (!ids.isEmpty()) {
                        int id = ids.get(RAND.nextInt(ids.size()));
                        er.updateSeverity(id, RAND.nextInt(10) + 1);
                    }
                    break;
                case 3:
                    er.treatNextPatient();
                    break;
                case 4:
                    List<Integer> ids2 = er.getAllPatientIds();
                    if (!ids2.isEmpty()) {
                        int id = ids2.get(RAND.nextInt(ids2.size()));
                        er.removePatient(id);
                    }
                    break;
            }

            System.out.println("\n--- STATE after step " + i + " (size=" + er.size() + ") ---");
            er.viewWaitingList();
            er.viewNextToTreat();
            System.out.println("-----------------------------------------------------\n");

            Thread.sleep(delayMs);
        }
        System.out.println("Auto-Simulation finished.");
    }

    private static String randomName() 
    {
        return SAMPLE_NAMES[RAND.nextInt(SAMPLE_NAMES.length)];
    }

    private static int safeNextInt(Scanner sc, int defaultVal) {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid integer: ");
            sc.next();
        }
        return sc.nextInt();
    }
}

