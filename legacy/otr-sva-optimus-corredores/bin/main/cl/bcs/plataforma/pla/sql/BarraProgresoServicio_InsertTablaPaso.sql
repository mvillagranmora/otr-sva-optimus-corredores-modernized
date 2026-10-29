
INSERT INTO "BSCL.Plataforma.BD::PLA.BarraProgresoPaso"(
  "IdTrabajo",  
  "FechaRegistro",
  "Servicio",
  "Progreso",
  "JsonData",
  "Estado",
  "IdUsuario"
)
VALUES(
	:IdTrabajo,
    CURRENT_UTCTIMESTAMP,
    :Servicio,
    :PorcentajeProgreso,
    :JsonData,
    :Estado,
    :IdUsuario
)




