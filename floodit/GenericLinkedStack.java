@SuppressWarnings( "unchecked" )
public class GenericLinkedStack<E> implements Stack<E> {

    // E is the type of the elements of this stack.  The specific type
    // will specified when a reference is declared and a stack is
    // actually created. E.g.:
    //
    // Stack<Integer> nums;
    // nums = new GenericArrayStack<Integer>( 10 );
    //Student name: Miléna Dionne 
    //Student ID : 8916596  
    //Section: C 
    //Yuhan Lee (#7312641) Section B 
    // Instance variables

    //private E[] elems; // Used to store the elements of this ArrayStack
    //private int top; // Designates the first free cell
     
    private static class Elem<T> {

        private T info ;
        private Elem<T> next ;

        private Elem (T info , Elem<T> next ){
            this.info = info ;
            this.next = next ; 
        }
    } 

    private Elem<E> top ; 

    // Constructor
    public GenericLinkedStack(){
        top = null ;
    }
   

    // Returns true if this ArrayStack is empty
    public boolean isEmpty() {
        return top == null;
    }

    public void push( E elem ) {
    // pre-condition: ! isFull()
        //System.out.println("call  ");
        if (elem == null ){
            throw new EmptyStackException();
    }
    top = new Elem<E>(elem, top);   

    }
    public E pop() {
    // pre-condition: ! isEmpty()
        if (isEmpty()){
            throw new EmptyStackException();
        }
        E tmp1 = top.info ; 
        top = top.next;
    //tmp1.next = null ;
    return tmp1 ;
    }

    public E peek() {
        if (isEmpty()){ 
            throw new EmptyStackException();
        }
    // pre-condition: ! isEmpty()
    return top.info;
    }

    public String toString() {
        if (isEmpty()) {
            return "Empty Stack";
        } else {
            return recursiveToString(top);
        }
    }


    private String recursiveToString(Elem<E> startNode) {
        if (startNode == null) {
            return "";
        }
        String separator = "";
        if (startNode != top) {  // add :: after each item (but not at start)
            separator = " :: ";
        }
        return separator + startNode.info + recursiveToString(startNode.next);
    }
}