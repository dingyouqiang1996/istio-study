
// Simple test program to verify that the Chilkat Java class
// library is ready to use.  It verifies that the Chilkat DLL 
// can be loaded and that a Chilkat object can be instantiated.
	
import com.chilkatsoft.CkZip;

public class Test {
	
  static {
    try {
    	
    	System.loadLibrary("chilkat");
    	
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load.\n" + e);
      System.exit(1);
    }
  }

	
  // Instantiate a Chilkat object and print it's version.
  public static void main(String argv[])
  	{
    CkZip zip = new CkZip();
    System.out.println(zip.version());
	}
  }

