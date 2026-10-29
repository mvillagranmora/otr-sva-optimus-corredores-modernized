SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" 
AS 

	SELECT 
		"TAR"."IdTarjetaBancaria",
		"TAR"."IdBanco",
		"BAN"."NombreBanco",
		'['|| "BAN"."NombreBanco" ||']'||  ' N°' || "TAR"."NumeroTarjetaBancaria" 	AS "NombreTarjetaBancariaFull",		
		"TAR"."IdTipoTarjetaBancaria",
		"TIP"."NombreTipoTarjetaBancaria",
		LEFT("TAR"."NumeroTarjetaBancaria",0) || 'XXXXXXX' || RIGHT("TAR"."NumeroTarjetaBancaria",4) 	AS "NumeroTarjetaBancariaEnmasc",
		"TAR"."NumeroTarjetaBancaria",
		"TAR"."CodigoVerificacion",
		"TAR"."IdPersona" as "IdPersona",
		"TAR"."FechaIngreso",
		"TAR"."FechaCreacion",
		"TAR"."FechaModificacion",
		"TAR"."FechaVencimiento",
		--"TAR"."IdEstado",
		"BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA',"TAR"."IdEstado","TAR"."IdEstadoAutorizacion") AS "IdEstado",
		"EST"."Nombre" as "NombreEstado",
		"TAR"."IdEstadoAutorizacion"
	FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS "TAR"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS "TIP" on "TIP"."IdTipoTarjetaBancaria" = "TAR"."IdTipoTarjetaBancaria"
	LEFT OUTER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS "BAN" ON "TAR"."IdBanco" = "BAN"."IdBanco"
	LEFT OUTER JOIN "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" AS "ESTSOC" ON "BSCL.SocioNegocio.BD.Utilitarios::SOC.ObtenerEstadoAutorizacion"('CUENTABANCARIA',"TAR"."IdEstado","TAR"."IdEstadoAutorizacion") = "ESTSOC"."IdEstado" AND "NombreEntidad" = 'CUENTABANCARIA'
	LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "ESTSOC"."IdEstado" = "EST"."IdEstado"
	WHERE "TAR"."Borrado"='N'
	WITH READ ONLY; 
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" TO  BCS_DESARROLLADOR WITH GRANT OPTION;








