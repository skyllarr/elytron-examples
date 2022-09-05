package manual.test;

import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.Stateful;

@Stateful
@Remote()
public class SimpleSecuredEJBImpl implements SimpleSecuredEJB {

    @PermitAll
//    @RolesAllowed("RunAsLoginModuleRole")
    public boolean accessRunAsLoginModuleRole() {
        return true;
    }

    @PermitAll
//    @RolesAllowed("RunAsLoginModuleRole")
    public boolean accessRunAsLoginModuleRole2() {
        return false;
    }
}
