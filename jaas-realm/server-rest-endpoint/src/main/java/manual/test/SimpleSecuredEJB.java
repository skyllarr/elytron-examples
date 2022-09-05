package manual.test;

import javax.ejb.Local;
import javax.ejb.Remote;

@Remote
public interface SimpleSecuredEJB {
    boolean accessRunAsLoginModuleRole();
}
