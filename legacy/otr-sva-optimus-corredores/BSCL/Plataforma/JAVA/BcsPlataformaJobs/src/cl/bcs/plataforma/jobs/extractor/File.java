package cl.bcs.plataforma.jobs.extractor;

public class File {
	private String extension;
	private String format;

	public File() {	}
	
	public File(String pattern, String extension, String format) {
		this.extension = extension;
		this.format = format;	
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	@Override
	public String toString() {
		return "[format: " + this.format + ", extension: "+ this.extension + "]"; 
	}

}
