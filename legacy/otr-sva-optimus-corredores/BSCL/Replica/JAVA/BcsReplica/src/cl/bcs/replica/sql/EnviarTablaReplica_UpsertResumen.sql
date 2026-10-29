UPSERT "BSCL.Plataforma.BD::PLA.ResumenReplica"
("Tipo", "Nombre", "Estado", "Detalle", "FechaSistema", "UltimaActualizacion")
VALUES(:Tipo, :Nombre, :Estado, :Detalle, :FechaSistema, :UltimaActualizacion) WITH PRIMARY KEY;
