package custom.fileobserver;

public interface FileListener {

	void onFileAccess(String name);

	void onFileOpen(String name);

	void onFileCreated(String name);

	void onFileDeleted(String name);

	void onFileModified(String name);

	void onFileRenamed(String oldName, String newName);
}
