INSERT INTO "BSCL.Plataforma.BD::PLA.LogAccesoServicioVentana"
("IdVentana", "TituloVentana", "IdServicio", "IdUsuario", "FechaAcceso")
VALUES(:p_IdVentana, :p_TituloVentana, :p_IdServicio, :p_IdUsuario, CURRENT_TIMESTAMP);
