 INSERT INTO "BSCL.Plataforma.BD::IND.Indicador"
("IdIndicador", "IdEntidad", "IdTipoEntidad", "IdTipoIndicador", "ModeloCliente", "IdEmpresa", "ValorIndicador", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdEstadoAutorizacion", "FechaVencimiento", "IdCarga")
 SELECT ("BSCL.Plataforma.BD.Secuencias::Indicador".NEXTVAL) AS "IdIndicador", SOC."IdCliente" AS "IdEntidad", 'CLIENTE' AS "IdTipoEntidad", HCI."IdTipoIndicadorDestino" AS "IdTipoIndicador", IND."ModeloCliente", IND."IdEmpresa", IND."ValorIndicador", CURRENT_UTCTIMESTAMP AS "FechaIngreso", CURRENT_UTCTIMESTAMP AS  "FechaCreacion", CURRENT_UTCTIMESTAMP AS "FechaModificacion", IND."IdEstadoAutorizacion", IND."FechaVencimiento", IND."IdCarga"
 FROM "BSCL.Plataforma.BD::IND.Indicador" IND
 INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIN ON (IND."IdTipoIndicador"=TIN."IdTipoIndicador" AND IND."IdTipoEntidad"=TIN."IdTipoEntidad" AND IND."IdEmpresa"=TIN."IdEmpresa")
 INNER JOIN --Identificacion de la herencia
 		(
 		 	SELECT  HER."CodigoIndicadorOrigen", HER."IdTipoEntidadOrigen", HER."CodigoIndicadorDestino", HER."IdTipoEntidadDestino",TIX."IdTipoIndicador" AS "IdTipoIndicadorDestino", HER."IdEmpresa"
  			FROM "BSCL.Plataforma.BD::IND.HerenciaIndicador" HER
 			INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIX ON (HER."CodigoIndicadorDestino"=TIX."CodigoTipoIndicador" AND HER."IdEmpresa"=TIX."IdEmpresa" AND HER."IdEstado"='ACT')
 		) HCI ON (HCI."IdEmpresa"=TIN."IdEmpresa" AND HCI."CodigoIndicadorOrigen"=TIN."CodigoTipoIndicador" AND HCI."IdTipoEntidadOrigen"=TIN."IdTipoEntidad" )
 INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON (IND."IdEntidad"=SOC."IdPersona" AND COALESCE(SOC."IdCliente",0)>0 )
 WHERE IND."IdEntidad"=:IdPersona AND IND."IdTipoEntidad"='PERSONA' AND IND."IdEmpresa"=:IdEmpresa AND HCI."IdTipoEntidadDestino"='CLIENTE';