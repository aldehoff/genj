package genj.reportrunner;

/**
 * Exception while working with a report proxy object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: ReportProxyException.java,v 1.1 2008-11-15 23:27:40 pewu Exp $
 */
public class ReportProxyException extends Exception
{
    public ReportProxyException(String arg0)
    {
        super(arg0);
    }

    public ReportProxyException(Throwable arg0)
    {
        super(arg0);
    }
}
