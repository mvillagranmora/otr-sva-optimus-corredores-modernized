INSERT INTO "BSCL.Plataforma.BD::PLA.LogServicio"
("IdModulo","IdUsuario","FechaHora","TipoMensaje","Glosa","Valor1","Valor2")
VALUES (:IdModulo,:IdUsuario,CURRENT_TIMESTAMP,:TipoMensaje,:Glosa,:Valor1,:Valor2);