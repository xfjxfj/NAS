package custom.fileobserver;

public interface FileListener {

	void onFileOpen(String path);

	void onFileCreated(String path);

	void onFileDeleted(String path);

	void onFileModified(String path);

	void onFileRenamed(String oldPath, String newPath);
}
