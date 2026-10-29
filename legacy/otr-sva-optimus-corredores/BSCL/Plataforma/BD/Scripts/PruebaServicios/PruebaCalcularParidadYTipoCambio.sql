SET SCHEMA "BSCL";




DELETE FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 1,0,'2017-02-21','CLP','MXN',0,1000,'CARTERA' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,1,'2016-12-05','UF','USD',0,65000,'ORDENES' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,2,'2016-01-01','UTM','CLP',0,1,'VENTA MILA' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,3,'2016-12-15','USD','CLP',0,1,'VENTA EXTRANJEROS' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,4,'2016-12-15','USD','CLP',0,1,'VENTA EXTRANJEROS' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,5,'2016-12-15','CLP','CLP',0,1,'VENTA EXTRANJEROS' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
--INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio_IN"
SELECT 0,6,'2016-12-15','USD','USD',0,1,'VENTA EXTRANJEROS' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
SELECT 0,7,'2016-12-17','EUR','EUR',0,1,'VENTA EXTRANJEROS' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
SELECT 0,8,'2016-12-05','CLP','EUR',0,10000,'ORDENES' FROM DUMMY;


CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
	p_IdEmpresa		=> 1,
	p_IdTrabajo		=> 1);


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
SELECT * FROM "BSCL"."BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida"

rollback;

select * from "BSCL"."BSCL.Plataforma.BD::MER.ParidadYTipoCambio" where "IdMoneda" = 'USD'
