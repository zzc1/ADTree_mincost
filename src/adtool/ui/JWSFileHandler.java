
package adtool.ui;

import java.awt.Frame;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

//Provides static methods for choosing file to open or save.
public class JWSFileHandler extends FileHandler {

	public JWSFileHandler(Frame window) {
		this(null, window);
	}

	public JWSFileHandler(final StatusLine status, final Frame window) {
		super(status, window);
	}

	public ObjectInputStream getLoadLayoutStream() {
		ObjectInputStream in = null;
		FileOpenService fos = null;
		FileContents fileContents = null;
		try {
			fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
		} catch (UnavailableServiceException exc) {
			statusLine.reportError("Open command failed: " + exc.getLocalizedMessage());
		}

		if (fos != null) {
			try {
				fileContents = fos.openFileDialog(null, null);
			} catch (Exception exc) {
				statusLine.reportError("Open command failed: " + exc.getLocalizedMessage());
			}
		}

		if (fileContents != null) {
			try {
				// This is where a real application would do something
				// with the file.
				in = new ObjectInputStream(fileContents.getInputStream());
				statusLine.reportError("Opened file: " + fileContents.getName() + ".");
			} catch (IOException exc) {
				statusLine.reportError("Problem opening file: " + exc.getLocalizedMessage());
			}
		} else {
			statusLine.reportWarning("User canceled open request.");
		}
		return in;
	}

}
