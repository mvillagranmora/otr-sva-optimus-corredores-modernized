SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionUI";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionUI"
AS
 SELECT
	  REC."IdRecaudacion"
	 ,REC."IdEmpresa"
	 ,REC."IdUsuario"
	 ,REC."IdTraspaso"
	 ,REC."FechaDeposito"
	 ,REC."IdMedioDePago"
	 ,REC."IdBanco"
	 ,REC."NumeroCuentaBancaria" 
	 ,REC."Sucursal" 
	 ,REC."MontoDeposito" 
	 ,REC."Observacion" 
	 ,REC."IdEstado" 
	 ,REC."FechaIngreso" 
	 ,REC."FechaIngresoRegistro" 
	 ,MP."Nombre"  			AS "MedioDePago"
	 ,BAN."NombreBanco"	 	AS "Banco"	 
 FROM "BSCL.FondoMutuo.BD::TRA.Recaudacion" 						AS REC
 INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" 	AS MP 	ON (MP."IdMedioDePago" = REC."IdMedioDePago")
 LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" 							AS BAN 	ON (BAN."IdBanco" = REC."IdBanco");
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarRecaudacionUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO