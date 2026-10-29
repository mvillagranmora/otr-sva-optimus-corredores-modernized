SELECT COUNT(1) AS \"Total\" from \"BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina\"  AS NOM"
                + " INNER JOIN \"BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente\" CER ON  NOM.\"IdPortafolio\"=CER.\"IdPortafolio\" AND NOM.\"Periodo\"=CER.\"Periodo\""
                + " WHERE   NOM.\"Notificar\"='SI' AND NOM.\"Periodo\"= ? AND CER.\"IdTipoDocumento\"=?