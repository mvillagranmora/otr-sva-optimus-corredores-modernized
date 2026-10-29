SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoOrigenAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoOrigenAPV"  AS 

SELECT Traspaso."IdTraspaso"
	 , Traspaso."IdContratoSAAPV"
	 , Traspaso."TipoMovimiento"
	 , Traspaso."RazonRetiro"
	 , Traspaso."FechaIngreso"
	 , Traspaso."CodigoTipoTraspaso"
	 , Traspaso."IdCliente"
	 , Traspaso."IdPersona"
	 , Traspaso."Descripcion"
	 , SocioNegocioCli."IdSocioDeNegocio" AS "IdSocioNegocioCli"
	 , COALESCE(SocioNegocioEmp."IdSocioDeNegocio",0) AS "IdSocioNegocioEMP"
	 , EntidadRecaudadora."Codigo"					  AS "CodigoEntidadRecaudadora"
	 , DetalleOrigenTraspaso."IdDetalleOrigenTraspaso"
	 , DetalleOrigenTraspaso."CodigoTipoAhorro"
	 , DetalleOrigenTraspaso."IdEntidadRecaudadora"
	 , DetalleOrigenTraspaso."NombreEntidad"
	 , DetalleOrigenTraspaso."DocumentoEntidad"
	 , DetalleOrigenTraspaso."Fondo"
	 , DetalleOrigenTraspaso."MontoTraspaso"
	 , DetalleOrigenTraspaso."UFTraspaso"
	 , DetalleOrigenTraspaso."PorcentajeTraspaso"
	 , DetalleOrigenTraspaso."IdEstado"
	 , DetalleOrigenTraspaso."CodigoRegimenTributario"
	 , Portafolio."CodigoPropositoPortafolio"
	 , Portafolio."IdPortafolio"
	 , Instrumento."IdInstrumento"
	 , Instrumento."Nemotecnico"
  FROM "BSCL.APV.BD::TRA.Traspaso" AS Traspaso
  LEFT OUTER JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS DetalleOrigenTraspaso
			   ON DetalleOrigenTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
  LEFT OUTER JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora" AS EntidadRecaudadora
  			   ON EntidadRecaudadora."IdEntidadRecaudadora" = DetalleOrigenTraspaso."IdEntidadRecaudadora"
  LEFT OUTER JOIN "BSCL.APV.BD::TRA.RazonRetiro" AS RazonRetiro
			   ON RazonRetiro."Codigo" = Traspaso."RazonRetiro"
  LEFT OUTER JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS Contrato
 		 	   ON Contrato."IdContratoSAAPV" = Traspaso."IdContratoSAAPV"
 		 	  AND Contrato."IdCliente" = Traspaso."IdCliente"
 			  AND Contrato."CodigoTipoSolicitud" = 'TRA' 			
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioCli
			   ON SocioNegocioCli."IdCliente" = Traspaso."IdCliente"
			--AND SocioNegocioCli."CodigoTipoPersona" = 'N'
  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" AS SocioNegocioEmp
			   ON SocioNegocioEmp."IdPersona" = Traspaso."IdPersona"
			  AND SocioNegocioEmp."IdEmpresa" = Traspaso."IdEmpresa"
  LEFT OUTER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio
               ON Portafolio."IdPortafolio" = DetalleOrigenTraspaso."IdPortafolio"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" Instrumento
               ON Instrumento."IdInstrumento" = DetalleOrigenTraspaso."IdInstrumento";
			
GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarTraspasoOrigenAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;