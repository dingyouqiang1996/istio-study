
	
import com.chilkatsoft.CkHttp;

public class HttpWithEvents {
	
  static {
    try {
    	
    	System.loadLibrary("chilkat");
    	
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load.\n" + e);
      System.exit(1);
    }
  }


	
  public static void main(String argv[])
  	{
    CkHttp http = new CkHttp();
    
    // Install an event callback handler to get progress events.
    MyHttpProgress myProgress = new MyHttpProgress();
    http.put_EventCallbackObject(myProgress);
    
    boolean success;

    //  Any string unlocks the component for the 1st 30-days.
    success = http.UnlockComponent("Anything for 30-day trial");
    if (success != true) {
        System.out.println(http.lastErrorText());
        return;
    }

    //  Download a file at a URL.
    success = http.Download("http://www.chilkatsoft.com/download/9.5.0.51/ChilkatDotNet45-9.5.0-x64.zip","ChilkatDotNet45-9.5.0-x64.zip");
    if (success != true) {
        System.out.println(http.lastErrorText());
        return;
    }
    
    System.out.println("OK");
  	}
  }

