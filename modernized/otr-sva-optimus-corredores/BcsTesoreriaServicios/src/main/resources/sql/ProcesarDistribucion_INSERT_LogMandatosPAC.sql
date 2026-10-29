INSERT INTO "BSCL.Tesoreria.BD::PAC.LogMandatoPAC"
("IdLog", "IdentServicio", "IdMandatoPAC", "FechaProceso", "FechaSistema", "Usuario", "EstadoProcesamiento", "PreRendicion", "Glosa") 
VALUES
("BSCL.Tesoreria.BD.Secuencias::PAC.IdLog".NEXTVAL, :IdentServicio, :IdMandatoPAC, CURRENT_TIMESTAMP, :FechaSistema, :Usuario, :EstadoProcesamiento, :PreRendicion, :Glosa);