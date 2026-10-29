package cl.bcs.plataforma.jobs;

import java.sql.Connection;

import cl.bcs.plataforma.evt.EnviarEmail;
import cl.bcs.plataforma.evt.EnviarEmail.EnviarEmailIn;
import cl.bcs.plataforma.evt.EnviarEmail.EnviarEmailOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;

public class DespachadorEmailsJob extends BcsJob {

  @Override
  public boolean transaction(Connection conn) throws InterruptedException, Throwable {
    EnviarEmail enviarEmails = new EnviarEmail();
    EnviarEmailOut out = enviarEmails.logica(conn, new EnviarEmailIn());
    return out.resultado;
  }

  @Override
  public void onError(Throwable t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }
  
//solo para test
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				DespachadorEmailsJob apf = new DespachadorEmailsJob();
				System.out.println(apf.transaction(con));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
