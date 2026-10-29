INSERT INTO "BSCL.Normativa.BD::CAR.CartolaHistoricaMercados"
("IdCartolaHistoricaMercados",
"IdCliente",
"Fecha",
"NombreMercadoCartola",
"MontoTotalMercado",
"PorcentajeTotalMercado")
VALUES(
"BSCL.Normativa.BD.Secuencias::CAR.CartolaHistoricaMercados".NEXTVAL,
:IdCliente,
:Fecha,
:NombreMercadoCartola,
:MontoTotalMercado,
:PorcentajeTotalMercado);