A java backend program which demonstrates the working of a Hospital Emergency Management Room in Real World Environment
<br>
Designed an automated ER system based on Linked Lists, Priority Queues, and Heaps to sort patients according to severity and arrival time. 
<br>
The system sorts the most critical patients first and processes data optimally , storing records in txt file using file handling.
<br><br>
Scope
<br>
• Add, remove, treat patients<br>
• Track arrival order<br>
• Maintain real-time priority using a heap-based priority queue<br>
• Update severity in case of Emergency
<br><br>
Data Structures Usage
<br>
• Linked List -> Arrival order tracking, sequential view<br>
• Priority Queue & Heap -> Triage, severity-based patient selection<br>
• HashMap -> Fast patient lookup by ID<br>
• Array / ArrayList -> Auto-simulation, temporary storage
<br><br>
Time Complexity
<br>
• Add Patient → O(log n) due to PQ insertion.<br>
• Treat Next Patient → O(log n) due to PQ removal.<br>
• Search by ID → O(1) using HashMap.<br>