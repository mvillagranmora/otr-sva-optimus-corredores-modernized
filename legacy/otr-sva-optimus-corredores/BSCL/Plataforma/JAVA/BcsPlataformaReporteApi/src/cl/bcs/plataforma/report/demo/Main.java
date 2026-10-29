package cl.bcs.plataforma.report.demo;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	final static String DB_USER     = "APPJAVA";
	final static String DB_PASSWORD = "Bolsa123";
	
	/*private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {

		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		
		//int twips = pixelsToTwips(128);
		//int pixels = twipsToPixels(1920);
		  
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		return resizedImage;
	}
	
	private static int twipsToPixels(int twips) {
		int ppi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		return twips *  ppi / 1440;
	}
	
	private static int pixelsToTwips(int pixels) {
		int ppi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		return pixels * 1440 / ppi ;
	}*/

	public Main() {
		final String REPORT_NAME = "C:\\BCS\\Reportes\\template_ordenes.rpt";
		/*
		String sourceImage = "data:image/png;base64,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";
		String imageBase64 =  sourceImage.split(",").length > 1 ? sourceImage.split(",")[1] : sourceImage;
		byte[] imageByte = Base64.getDecoder().decode(imageBase64);
		ByteArrayInputStream bais = new ByteArrayInputStream(imageByte);
		BufferedImage image = null;
		try {
			image = ImageIO.read(bais);
			bais.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (image != null) {
			BufferedImage resizeImage = resizeImage(image, image.getWidth()/4, image.getHeight()/4);//image.getType());
			File outputImage = new File("C:\\BCS\\Reportes\\abc2.png");
			try {
				ImageIO.write(resizeImage, "png", outputImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

		try {
			Connection conn = null;
			try {
				String hanaSchema = "BSCL";
				String hanadb = "jdbc:sap://161.238.34.246:30215/" + hanaSchema;
				conn = DriverManager.getConnection(hanadb, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
	            stmt.execute("SET SCHEMA \"" + hanaSchema + "\"");
			} catch (SQLException e) {
				System.err.println("Connection Failed. User/Passwd Error?");
			}
			
			// Archivo de reporte
			File rep4 = new File(REPORT_NAME);
			System.out.println("Report rpt : " + rep4.getAbsolutePath());
			
			// Carga de un reporte en el sistema (DB)
			//ReportDocument reportTest1 = new ReportDocument(conn);
			//String loadRep4 = reportTest1.loadDocument("TEMPLATE_ORDEN-PDF", rep4 );
			//System.out.println("ID: "+loadRep4);
			/*
			// Abre y lee un reporte desde el sistema (DB)
			ReportDocument reportTest2 = new ReportDocument(conn);
			boolean isOK = reportTest2.readDocument("57C87BB4891E0F1CE10000007F000002");
			
			// Definición de los dataSources que utilizará el reporte
			String call0 = " call BSCL.\"POC.ARQ.BD::TestCR\"( 1,34986) ";
			String query0 = 
					" select \"IdPortafolio\", \"IdOrden\", \"FechaIngresoRegistro\", \"FechaVigencia\" " +
					" from BSCL.\"BSCL.Orden.BD.Vistas::ConsultarOrden\" " +
					" where \"IdPortafolio\" = 34986 ";

			// Map de dataSources para los subreportes
			Map<String, String> subdocsQuery = new HashMap<String, String>();
			subdocsQuery.put("ordenes.rpt", query0);
			
			// Asignación de los dataSources al reporte principal y a sus subreportes 
			reportTest2.setDocumentResultSet(call0);
			reportTest2.setSubdocumentsResultSetFromQuery(subdocsQuery);
			
			String sourceImage0 = "data:image/png;base64,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";
			String sourceImage1 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/4QBGRXhpZgAATU0AKgAAAAgABAESAAMAAAABAAEAAFEQAAEAAAABAQAAAFERAAQAAAABAAALE1ESAAQAAAABAAALEwAAAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCACAASQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9/KKM0m8eooC4tFG6jdQFwoo3UbqAuFFG6jdQFwoo3UbqAuFFG6jdQFwoo3UbqAuFFJvHqKXNAXCiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooA/CX9vD9vDxV+1h8WNY/4nF9aeC7S6kg0nSYJWjt/IViFlkUHDyuBuJbON2BgACvn2iv6V1CiNflr2a1ZYe0Yo+aw1CWJlKUpa/fufzUUV/S1hf9n8qje4jh+8yL9WrknmkYK81b5nbHKJdJ/h/wT+amiv6Vo7iKb7rRt9DUoX6flRDNIzV4K/z/AOAH9kyW8/w/4J/NJRX9LmKMVp/aH938f+AL+yX/AD/h/wAE/mjor+lzFGKP7Q/u/j/wA/sl/wA/4f8ABP5o6K/pcxRij+0P7v4/8AP7Jf8AP+H/AAT+aOiv6XMUhUUf2h/d/H/gB/ZL/n/D/gn80lfQP7Bv7d3ir9kz4saP/wATa+uvBd1cxwatpE0zSW/kMwDSxqThJEB3ArjO3ByCRX7rsimNvl4r+aetqNZV7xkjkxGHlhnGUZa/dsf0uDpRSL90fSlrxz6RBRRRQAUUV5j+1f8AtMaT+zH8LbrVru4gbWryKSHRbFl8xry5C/KSgZT5SEqZGyMKQAdzIra0aM6tRU6au3sZ1akacHObskdx4w8d6H8PdMjvdf1nSdDs5ZRAk+oXcdtE8hBYIGcgFiFY464U+leR/wDDyT4K/wDQ6f8AlIvv/jNfGPgD4OfEn/gpN8UtT8TaheQWthDKkFzqNwji0sY9wItbWMZ3MiMX2ZA5zI4aQM30dpf/AAR9+H0WmW63viLxlcXixKJ5YJ7aGKSTA3MqGFiqk5IUsxA43HrX0VTK8uwv7vGVW59VHp+D/T0PGjj8bX9/DU1y9HLr+KPQP+HknwV/6HT/AMpF9/8AGaP+HknwV/6HT/ykX3/xmuG/4dA/DX/oOeOf/Ay1/wDkej/h0D8Nf+g545/8DLX/AOR6z9jkn89T8P8AIv2mafyw/H/M7n/h5J8Ff+h0/wDKRff/ABmj/h5J8Ff+h0/8pF9/8Zrhv+HQPw1/6Dnjn/wMtf8A5Ho/4dA/DX/oOeOf/Ay1/wDkej2OSfz1Pw/yD2mafyw/H/M7n/h5J8Ff+h0/8pF9/wDGaP8Ah5J8Ff8AodP/ACkX3/xmuG/4dA/DX/oOeOf/AAMtf/kej/h0D8Nf+g545/8AAy1/+R6PY5J/PU/D/IPaZp/LD8f8zuf+HknwV/6HT/ykX3/xmj/h5J8Ff+h0/wDKRff/ABmuG/4dA/DX/oOeOf8AwMtf/kej/h0D8Nf+g545/wDAy1/+R6PY5J/PU/D/ACD2mafyw/H/ADO5/wCHknwV/wCh0/8AKRff/Ga6T4cftm/C/wCLGp/YtE8ZaTJeGWKCOC732MtxJISqJEs6oZWJGMJuIJXONwz5F/w6B+Gv/Qc8c/8AgZa//I9cb8VP+CO1s9u83gjxZPHKsSBLPXIw6yyb/mY3EKjYuw8L5LHcv3sN8rWGyWfuxqzi+7St+CF7bM46unFrsnr+Z9u0V+dP7LH7Zfin9kb4j3HgH4nDVm0G1lSzlju2ae48PsqqqNFyd9tsCfIhK7NrxZ5WT9D9L1S21vTLe9sriC8s7yJZ4J4JBJFPGwDK6sOGUgggjgg15uZZbUwc0paxeqktmjuwWOhiY3jo1unuieiiivNO0/mjr+loD5K/mlr+le5l8u2Zv7qk16GbT5I876X/AEPEyVX5vl+p4b+1d+1cvwdhXR9HWKfXrhN7FuUs0PRmHdj2X8TxgN8e+LviVr3j+7kuNY1bUNQkkOSskp8tfZUHyqPYACl+JPi+bx3491bVp5Gke/umkXPZM4QD2VQoHsK+4v2d/wBnfR/hL4Os2azt59amhV7u7dQz7jglVJ5VQeABjOMnmv4Y/wCF7xKz6vQoYh0cNRei1sldpNpNXbs3qz+nIxy3g7LKNWtR9piKnXS97JvVp2SulotT4c8IfErxB4BvY7jSNY1DT3jOQscp8s/7yH5WHsQRX2H+yj+1YvxjhbSdW8q3161j3qV4S8QdSo7MO6/iOMgb37RH7OujfFnwbeOlnBba1bxM9rdRqFkDgZCMR95D0IOcZyOa+Hvht4wm8CePtH1e3kaN7C7SRsd0zhwfZlLA+xoi8/8ADTPsPQxFf22FrPVa2auk2k27NXT0bFKOW8Y5ZWq0aPs8RT66b2bWqSunZrVaH6c0VHbSebbo3qAakr+06c1OKmup/P70dgooorQBoTFLuFcZ8afitb/CXwdLqE22S6kzFZ25PM0nv/sjqT6cdSK+Pf8AhYmtf8Jj/b39o3X9reb5vn7znOc7fTb229McYxW1Oi5q5z1sQoOzPvagjIrk/hB8SbX4qeCbXVLfasx/d3MIOfIlGNy/yI9iK6ysWmnZm0ZJq6Gv92v5pa/pak+7X80tellv2vkeNnH2fn+h/S2n3B9KWkT7g+lLXmntR2CkTp/niloHWgYV+cX/AAUa1jUvjh+2zpngW1b7L/Z/2HRLMXFy7Wxnu9khnKhT5efPjRtoYkQKeeFH6O1+cX7Q3/KWjT/+xm8P/wDouyr6PhmyxM59Ywk166Hi55rRjDo5JP01P0B+Gfw8034TfD/SPDejxeVp2i2yW0OVVXkwPmkfaFUyO2XZgBuZmPetyiivnpScpOUt2exGKiuVbBUd1dR2NrJNNJHDDCpeSR2CqigZJJPAAHOTRdXUdjayTTSRwwwqXkkdgqooGSSTwABzk183/F/4v6h8bPEMXh3w7FcSabJKEREG2TUHHO9s42xjGQDgADc2MAL+c+IviLgeFMCqlROriKr5aVKOsqknotFdqKbV3Z7pJOTSfvZHkdbMq3LH3YR1lJ7RX+fZfpdn0Zpmq2utWMd1Z3Fvd20mdksMgkjfBIOGHBwQR9RVivmHwZ4z1z9mfxxNpWqwyS6fKwaeBTlZFPAnhJwM4HtnG1sEAr9J6Frtn4m0i3v7C4jurO6XfHInRh/MEHIIPIIIOCK5vDjxKw3E9Gph68HQxtB2q0ZfFFp25lezcb6bXi9H0ctM9yGpl8lOD56U9YzWz8n5/nuuqVuiiiv00+fCivlX/gq1rvj7wd8KvD+teEdc1bRtIsb949YbTJpYLjdIoFu7SRgFYQRIpy4UvLEMMdpX1L9ij9oH/hpH9nvR9duJPM1mzzpur/LjN3EF3PwiL+8Ro5cINq+btySprz4ZhB4uWDaakkmuzXl6f59j0qmV1I4KOOTTi200t0+l/X/LuesUUUV6B5p8m/8ABWT4GW3i34O2vjm3WCHVPCcqQXLkBWubSeRYwmQhZmSZkKgsqqskx5LCuq/4Jf8AxIuviB+ynY29357S+Gb6fR1mmuDM08ahJo8ZHyqiTLEq5ICxDGBhRrf8FJP+TLPGn/bj/wCl1vXDf8Egf+Tatc/7Gaf/ANJbSvpeZ1MkfP8AZnZeWi/zZ4fKoZp7v2o6/f8A8BH1XRRRXzR7h/NHX9K9zF5lsy/3lIr+aiv6W/4a9DNoc8eR9b/oeJkunM/T9T8x/iV4Rm8BeP8AWNIuI2jawu3jXI6pnKH6MpUj2NfcP7O37RWj/FvwdZxveW9vrVvEsd1auwV94GC6g/eVuoIzjODzWD+1X+ykvxhiXV9IMdrr1qmxlf8A1d4nYMezDs34HjBHx54w+G+v+ArqS31fR9Q094zgtJEfLP8AuuPlYe4JFfww4594a59iMRQoOtha2z1s1dtJtJ2au1qj+nIyyzjHLKVGtW9niKfpfZJ6Nq6dk9HofcP7RX7ROj/CPwfeIl9b3GtXERjtbWNg0m4jG5gPuqOpJxnGBzXw98NPB8nj3x9o+kQRtJJf3aRNgfdTOXP0VQxPsKXwf8Ndf8fXiW+kaPqGoSSHAaOI+Wv+85+VR7kgV9hfspfspJ8IIm1fVmjuteuE2hV5js0PVVPdj3b8BxksWz3xLz/D169B0cLRer1sldNpNpXbslorClLLODssrUaNb2mIqem9mlom7JXb1ep7lax+TbovoAKkoor+06cFCKguh/P7d3cKzfEOv2vhnRLrULyZbe1s4zJLIx6AfzPYDueK0icCvlH9qv43/wDCa622g6XMzaTp8n76RT8t1MP5qvQdicnng10U6bm7GNasqcbs4n4yfFa7+LnjKbUJt0drH+6s7cniCPPH/Aj1J9fYCug1n9mfVtG+DkfiiTzPtWfPnstvzQ2xAw577h1I7KexBrY/ZS+B3/Ca6yviDVId2k6fJ+4jccXUw/mq9T2JwOcEV9Sy263ULRyKrxsCrKwyGB6giuipW5Gox6HLRw/tE5z6nxr+zt8X3+E/jVWuGb+yNQIiu0/uD+GQD1XP4gnvivsmC6juoUkjZZI5AGVlOQwPQg18a/tE/CB/hP41ZbdG/sfUCZrR8fcH8UZPqufxBHfNesfsffGT+29N/wCEV1GX/SrNC1g7HmSIdU+q9R/s/wC7RXgpL2kSsPUcJeyke8P92v5pa/pak+7X80tbZb9r5HFnH2fn+h/S2n3B9KWkT7g+lLXmntR2Cmp0/wA806igYV+cX7Q3/KWjT/8AsZvD/wD6Lsq/R2vzi/aG/wCUtGn/APYzeH//AEXZV9Hw1/Gq/wDXuX5o8XPP4dP/ABr9T9HajurqOxtZJppI4YYVLySOwVUUDJJJ4AA5yakrw39sTWdcto7CzjWSPw9cKDJIg4mnDE7HOegUKwBwCcnkr8v5R4gcYR4YyKvnMqUqvs0rRj3bsuZ/Zjdrml0Wybsj6/JcreYYyGFUlHm6vst7d32Rzvxf+L+ofGzxDF4d8OxXEmmyShERBtk1BxzvbONsYxkA4AA3NjAC+ufBb4LWfwn0jc3l3WsXSgXNyBwo6+XHnkID36sRk4wAvP8A7J+ieHoPCEl9p8n2nWZP3d+0qhZbfuI1GTiM4yGH3yOcFdq+sV+aeE/Bs8wnHjziCrHE4zEK9PladOjB3tCG6Ulqpfyu8btucpe/xJmioJ5PgounSg9b6Ob7vy6rvo9rJc18Ufhfp/xU8PGzvB5VxFlrW6VcyWznuPVTgZXoQOxAI8G8GeM9c/Zn8cTaVqsMkunysGngU5WRTwJ4ScDOB7ZxtbBAK/T1cJ+0Nonh7U/h7PNr8n2X7Lk2dxGoadJSOFQZG7djlcgEDJK7dy+h4rcB+1/4y7JaywuPwqcvaNqMakIrWFRvTbRSlpb3Ze7Zxx4bzjl/4TMXF1KNR25d2m+seu/Req137DQtds/E2kW9/YXEd1Z3S745E6MP5gg5BB5BBBwRVuvm/wDZH1nXI/GctjaLJNocitJfBhmO3badjg54dmAXAzuXPB2gr9IV9X4XcdPi3IKebToulK7jJNPlco2vKDe8HfTs04ttxbfm8Q5P/ZmNeGUuZbrvZ7J9n+as+pg/E/4c6Z8Xfh7rHhnWIvN03WrV7WbCozx5HyyJvVlEiNh1Yg7WVT2r85/2Z/GN9+wN+3DqHhLXr3bod3dDRtRmkdY4Wikw9pesvm+XHgPG5LsxjilmGN2a/TavjH/gr5+z9/wkXgjS/iNYx5uvD+3TdU+b71pJJ+5f5nAGyZyuEQs32nJIWOvpM+w0lCONo/HT19V1X6+lzs4axcHUll9f+HVVvSXR/p627H2dRXgP/BO79p2P9oj4H29nfXFxP4p8JxRWOrNKrkzqd4gn8xmbzGkSM7ySG8xZDtClSffq9jC4iFelGtT2kv6+7Y8DGYWphq0qFVaxdv8Ag/PdHhv/AAUk/wCTLPGn/bj/AOl1vXDf8Egf+Tatc/7Gaf8A9JbSu5/4KSf8mWeNP+3H/wBLreuG/wCCQP8AybVrn/YzT/8ApLaV9XR/5Ek/+vn6I+cqf8jSP+D9WfVdFFFfOHtH80df0tH7lfzS1/S0fuV6mZfZ+f6Hh5LtL5fqOqOS1jl+8in6ipKK8edOM1aaue4m1sRJbpGflRV+grzr47/Hmz+D+jeTD5d1rV0ube3LcRj/AJ6P6L6Dqx/Eg+O3x6s/hBo/kw+Xda1dKTb25PEY/wCej+i+g6sfxI+T/wDicfE/xf8A8vGqaxqkv1aRj+gAH0AA7AV0YXCxSulZHLiMR9mO4tx481q48TtrTapejVHbeblZSsg9gR0HsOMcYr1/4a/tqX2meXa+JrX+0IRx9rt1CTL7svCt+G38a774b/soeH9B8Ftaa1awatqV4A1xOcjyT2WIjBUD1GC3fjAHAfEr9iu+00SXXhm6/tCEZP2O4YJMvsr8K347fxrq9pSk+VnPGnWguZG78f8A9qDT7jwVHY+Gb77RdatGRLOgKtaxdCOcEO3I9hk91NeN/Bj4UXXxd8ZQ6fDujs4cS3lwBxDHnt/tHoB689AaybT4f61d+KI9FXS7xdUkcJ9neIq4yepz0Xvk8Y56V9jfBv4VWvwl8Gw6fDtku5MSXlwBzPJj/wBBHQD09yaJSjSjaO4U4yrTvPZHQaDoNr4Z0m2sLGFYbSzjEUca9AB/XuT1JrSoorhPSOU+L3w2tfip4JutLuNsc3+stpiP9RKPut9OoPsTXyF4e8JeJPDnxMtNPs7O7g16zul8tAh+Vg33if7ncnoV9q+5nbBo4U1pTrOCsYVMOpyUgk+7X80tf0tSfdr+aWu7LftfI8vOPs/P9D+ltPuD6UtIn3B9KWvNPajsFFFInT/PFAxa/OL9ob/lLRp//YzeH/8A0XZV+jtfnF+0N/ylo0//ALGbw/8A+i7Kvo+Gv41X/r3L80eLnn8On/jX6n6O1U13QrPxNpFxYX9vHdWd0uySNxww/mCDggjkEAjBFW6K+XrUadanKjWipRkmmmrpp6NNPRprRp7nuQnKElKLs1qmuh8w+M/Bmufsz+OIdV0qaSXT5WKwTsMrIp5MEwGBnA9s43Lgghfefhd8UdP+Knh4XlmfKuIsLdWrNmS2c9j6qcHDdCB2IIG1ruhWfibSLiwv7eO6s7pdkkbjhh/MEHBBHIIBGCK+bPGfgzXP2Z/HEOq6VNJLp8rFYJ2GVkU8mCYDAzge2cblwQQv8y47A5h4WZhLMstjKtk1aV6tJayw8npzwv8AZ6a6NWhN35Jv7+jWo8RUVQrtRxUV7stlNLo/P8t1pdH0R428baf8P/D02palN5VvFwqjmSZz0RB3Y4+gAJJABI+c/wDiof2p/iF/z62Nr9Wg06In8N0jY9ixH8Kr8p/xUP7U/wAQv+fWxtfq0GnRE/hukbHsWI/hVfl+jPBPgnT/AIfeHodN02HyreLlmPMkznq7nuxx9AAAAAABP+3+K2P+1RyOjLzjLFSi/vUE/u/x/wAN/ueHKPSWLkvVU0/1/P0+I8E+CdP+H3h6HTdNh8q3i5ZjzJM56u57scfQAAAAAAa1FFf0vgsFh8Hh4YXCwUKcElGKVkktEklskfA1q06s3UqO8nq292wqj4p8M2PjTwxqWj6nD9p03VrWWyu4d7J5sUiFHXcpDDKkjIIIzwRV6iulpNWZEZNO63PzC8HXV1/wTf8A29JLS+a4HhmSU2ktxKpY3Ok3DBkm3eVl2iKoz+Uo3SW8kYOCa/T2vk7/AIKv/s5/8LH+EVv420y18zWfB+ftflRZkuLBz8+dqFm8l8SDLKiI1wx5NT/8Esf2nZPi38KpvB2sXFu2ueDIoobQBUia504KEiO0Nl2iK7GYIAFaHJZmYn5jLZfUsZPAS+GXvQ/Vf128z6/No/2hgIZnD44+7P8AR/j+K7Hcf8FJP+TLPGn/AG4/+l1vXDf8Egf+Tatc/wCxmn/9JbSu5/4KSf8AJlnjT/tx/wDS63rhv+CQP/JtWuf9jNP/AOktpX6XR/5Ek/8Ar5+iPzWp/wAjSP8Ag/Vn1XRRRXzh7R/NHX9LR+5X80tf0tZ+SvUzL7Pz/Q8PJdpfL9RwPFea/Hf472fwf0cww+Xda5dLm3ty3EY/56P6L6Dqx/Ej0k/yr4Y8SeGfE/if4l3VjfWd5deILq4IkjKksxzjI7BAOh+6Fx2rhoU1J69D1MRUlGNo7szv+Jx8TvF//LxqmsapL9WkY/oAB9AAOwFfWPwH+A1n8H9G86ZVutcukxcXAHEY6+Wnoo7nqxGfQA+AXwHs/hBpPnTCO61y6TFxcAfLGOvlx+ijuerHnpgD0iqrVr+7HYjD4fl96W4UUUVznWGKKKKACiiigAooooAbJ92v5pa/pac5Sv5pa9LLftfI8TOPs/P9D+ltPuD6UtIn3B9KWvNPajsFItLRQMK/OL9ob/lLRp//AGM3h/8A9F2Vfo7X5t/tNapbaJ/wVVt729uILOzs/EOhTzzzyCOKCNYrNmdmPCqACSTwAK+k4Z1rVUv5Jfmjxc8/hU/8a/Jn6SUUUV82e0FVNd0Kz8TaRcWF/bx3VndLskjccMP5gg4II5BAIwRVuisq1GnWpyo1oqUZJppq6aejTT0aa0ae5UJyhJSi7NaproZvhPwfpvgfSFsdJtI7O1Vi+xSWLMepLEksegySeAB0AFaVFFRhMJQwtGOGw0FCEUlGMUlFJbJJWSS6JFVKk6k3UqNtvdvVv1YUUUV0GYUUUUAFfn74F/Zh8ffswf8ABQ/Tz4N8P6tceD72/GLtYpf7PTS5+ZopZfMI3QANtWV9zyQRPsJZFP6BUV5+Oy+GJcJSbTg7prfzXzPSy/NKmEVSEUnGcbNPbyfy1+88N/4KSf8AJlnjT/tx/wDS63rhv+CQP/JtWuf9jNP/AOktpXZf8FL9UttP/Y08VQ3FxBDLfS2UFskkgVriQXcMhRAfvMER2wOdqMegNcb/AMEgf+Tatc/7Gaf/ANJbSvtKX/Ikn/j/AER8nU/5Gkf8H6s+q6KKK+bPaP5o6/pXVlMa/NxX4Uft3fsIeK/2TPixq/8AxJ7668F3Vy82k6tBC0lv5DMSsUjAYSRAdpDYztyMgg18/wBezWorEWlFnzWFrywzlGUdfu2P6Wwwx1o3D2r+aSisP7P/AL34f8E7P7Wf8n4/8A/pc3Ubq/mjoo/s/wDvfh/wQ/tZ/wAn4/8AAP6XN1G6v5o6KP7P/vfh/wAEP7Wf8n4/8A/pc3Ubq/mjoo/s/wDvfh/wQ/tZ/wAn4/8AAP6XN1G6v5o6KP7P/vfh/wAEP7Wf8n4/8A/pc3Um4V/NJRR/Z/8Ae/D/AIIf2s/5Px/4B/SwzARt/jX809FfQX7B/wCwf4s/ay+K+j/8Se+tfBVrcpPq2rTwtHb+QrAtFGxGHkcDaAucbsnABNb0KKoJykzkxGIliXGMY6/fv8j92V+6PpS0DpRXjH0iCiiigAr4p/4K3/s93uu2GmfEjT/3sGi2y6Xq0bzKvkxGbMEiLgFv3kzq/wAxPzR4XAdh9rVBqml22t6ZcWV7bwXlneRNBPBPGJIp42BVkZTwykEgg8EGu7LsdLCYiNePTdd11OXGYWOIoulLr+Z81fsQf8FBNN+OGiweH/GF9Y6T40ttkMcsrrBDrm5gitH0UTlioaIfeJ3IMFkj+nK+Mf2mP+CUMfjTxbca18PNR0nQ1v5Q8+j3qPHaQMdxd4ZI1YqpOzEWzauWwyqFQeU6X+zL+1VommW9lZXHjKzs7OJYIIIPGMUcUEagKqKousKoAAAHAAr2quX5fin7bDVlTvvGWlvT+mjy6eMxmHXs69Nzts1rf1P0kor84v8Ahnn9rT/oIeOf/C1j/wDkqj/hnn9rT/oIeOf/AAtY/wD5KrL+waP/AEFU/vX+Zp/a1X/nxP7n/kfo7RX5xf8ADPP7Wn/QQ8c/+FrH/wDJVH/DPP7Wn/QQ8c/+FrH/APJVH9g0f+gqn96/zD+1qv8Az4n9z/yP0dor84v+Gef2tP8AoIeOf/C1j/8Akqj/AIZ5/a0/6CHjn/wtY/8A5Ko/sGj/ANBVP71/mH9rVf8AnxP7n/kfo7RX5xf8M8/taf8AQQ8c/wDhax//ACVR/wAM8/taf9BDxz/4Wsf/AMlUf2DR/wCgqn96/wAw/tar/wA+J/c/8j9Hao+JfFGm+DNFm1LWNQsdJ0622+ddXk6wQxbmCrudiFGWIAyeSQO9fnh/wzz+1p/0EPHP/hax/wDyVR/w79+P/wAcP+Rw1byf7L/49P8AhIvED32fM+/5XlGfb9xN2duflxuwcNZHhou9TFQt1s0391xPNK7VoUJX81ZfkQfth/tSat+3B8R9L8A+A9NnvtBhvt1hGIcXGr3IV1+0Nux5USo0mAxXCFnkI4WP7r/Zq+EH/ChfgX4b8JtP9pn0m2P2mQPvRp5HaWbYdqny/MdwuVB2hc5OTXKfsx/sQeDf2ZbRbizg/trxE21pNYvoUaaFvLKMLcY/cRtuk4BLEPhncBceyVz5pmFGdOOEwitTjrru33f9f5G+X4OpCcsRiHecvuS7BRRRXhnqBik2D0H5UtFABijFFFABijFFFABijFFFABijFFFABijFFFABijFFFABtHpRiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKAP/2Q==";
		//	reportTest2.setDocumentImage(sourceImage0,"pdf417");
			reportTest2.setDocumentImage(sourceImage1,"Picture1");
			reportTest2.setDocumentImage(sourceImage1,"testimage");
		//	reportTest2.setDocumentImage(sourceImage0,"testimafasdfge");
			
			String msg1 = "Lorem Ipsum es simplemente el texto de relleno de las imprentas y archivos de texto.";
			String msg2 = "Prueba de barcode PDF417 para la API de reportes.";
			reportTest2.setDocumentBarcodeImage("pdf417", msg2,"pdf417");
			
			File ex0 = reportTest2.exportDocumentToFile("pdf");
			if (ex0 != null) System.out.println("Export report pdf? : " + ex0.getAbsolutePath());
			*/
			/*
			File ex1 = reportTest2.exportDocumentToFile("excel");
			if (ex1 != null) System.out.println("Export report excel? : " + ex1.getAbsolutePath());
			
			File ex2 = reportTest2.exportDocumentToFile("csv");
			if (ex2 != null) System.out.println("Export report csv? : " + ex2.getAbsolutePath());
			
			File ex3 = reportTest2.exportDocumentToFile("word");
			if (ex3 != null) System.out.println("Export report doc? : " + ex3.getAbsolutePath());
			
			File ex4 = reportTest2.exportDocumentToFile("rtf");
			if (ex4 != null) System.out.println("Export report rtf? : " + ex4.getAbsolutePath());
			
			File ex5 = reportTest2.exportDocumentToFile("txt");
			if (ex5 != null) System.out.println("Export report txt? : " + ex5.getAbsolutePath());*/
			
		//	reportTest1.close();
		//	reportTest2.close();
			conn.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();			
		}
	}
	
	public static void main(String [] args) {
		new Main();
					
	}

}
