A java backend program which demonstrates the working of a Hospital Emergency Management Room in Real World Environment

Designed an automated ER system based on Linked Lists, Priority Queues, and Heaps to sort patients according to severity and arrival time. 

The system sorts the most critical patients first and processes data optimally , storing records in txt file using file handling.

Scope

• Add, remove, treat patients
• Track arrival order
• Maintain real-time priority using a heap-based priority queue
• Update severity in case of Emergency

Data Structures Usage

• Linked List -> Arrival order tracking, sequential view
• Priority Queue & Heap -> Triage, severity-based patient selection
• HashMap -> Fast patient lookup by ID
• Array / ArrayList -> Auto-simulation, temporary storage

Time Complexity

• Add Patient → O(log n) due to PQ insertion.
• Treat Next Patient → O(log n) due to PQ removal.
• Search by ID → O(1) using HashMap.