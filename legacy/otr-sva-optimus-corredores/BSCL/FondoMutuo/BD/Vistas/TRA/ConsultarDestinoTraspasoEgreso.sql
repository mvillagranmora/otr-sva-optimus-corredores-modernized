
SET SCHEMA BSCL; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarDestinoTraspasoEgreso";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarDestinoTraspasoEgreso" AS 

	SELECT 
	     FONDO."RunFFMM" ,
		 FONDO."NombreFFMM" ,	
		 FONDO."RutSociedadAdmFFMM" ,
		 FONDO."SociedadAdmFFMM" ,
		 FONDO."IdEstado",
		 COALESCE (DESTINO."IdDetalleTraspaso",0) AS "IdDetalleTraspaso",
		 COALESCE (DESTINO."Porcentaje",0) as "Porcentaje",
		 COALESCE(FONDO."IdMoneda",'CLP') AS "IdMoneda"
	FROM "BSCL.FondoMutuo.BD::FMM.FondosIndustria" AS FONDO
	LEFT JOIN "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino" DESTINO ON DESTINO."RunFFMM" =FONDO."RunFFMM";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::TRA.ConsultarDestinoTraspasoEgreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
