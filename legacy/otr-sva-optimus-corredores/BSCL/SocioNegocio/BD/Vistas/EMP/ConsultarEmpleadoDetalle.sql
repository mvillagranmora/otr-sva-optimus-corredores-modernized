SET SCHEMA "BSCL"; 
   
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoDetalle"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoDetalle" AS


SELECT  VIS."IdDocumentoIdentidad" , "VIS"."Tipo" , "VIS"."NombreCompleto" , "VIS"."Nombres" , "VIS"."ApellidoPaterno" , "VIS"."ApellidoMaterno" , "VIS"."RazonSocial" , "VIS"."IdGiroPrincipal" , "VIS"."IdPaisNacimiento" , "VIS"."IdPaisResidencia" , "VIS"."FechaNacimiento" , "VIS"."FechaDefuncion" , "VIS"."Sexo" , "VIS"."EsExtranjero" , "VIS"."EstadoCivil" , "VIS"."RegimenConyugal" , "VIS"."Cargo" , "VIS"."Empleador" , "VIS"."IdDireccionFisicaPrincipal" , "VIS"."IdDireccionElectronicaPrincipal" , "VIS"."IdTelefonoPrincipal" , "VIS"."IdProfesion" , "VIS"."DocumentoIdentidad" , "VIS"."TipoDocumentoIdentidad" , "VIS"."IdEmpleado" , "VIS"."IdPersona" , "VIS"."CodigoEmpleado" , "VIS"."IdEmpresa" , "VIS"."CodigoTipoCargoEmpleado" , "VIS"."NombreTipoCargoEmpleado" , "VIS"."IdEmpleadoSupervisor" , "VIS"."CodigoEmpleadoSupervisor" , "VIS"."IdSucursal" , "VIS"."NombreSucursal" , "VIS"."UtilizaPlantillaEmpleado" , "VIS"."IdPlantillaEmpleado" , "VIS"."FechaInicioCargo" , "VIS"."FechaTerminoCargo" , "VIS"."FechaIngreso" , "VIS"."FechaCreacion" , "VIS"."FechaModificacion" , "VIS"."CodigoAgenteMigracion" , "VIS"."CodigoSupervisorAgenteMigracion" , "VIS"."CodigoYNombreEmpleado" , "VIS"."EsEnrolado" , "VIS"."IdEstado" , "VIS"."DetalleCargo" , "VIS"."IdDireccionElectronica" , "VIS"."CargoIndefinido" , "VIS"."RestringeCarteraCliente" , "VIS"."RestringeOrdenCliente" , "VIS"."NombreEstado" 
,INDE."ValorIndicador"AS "IndEmpleadoCorredora"
,INDFEC."ValorIndicador" AS "FechaAcreditacion", INDPRIV."ValorIndicador" AS "InformacionPrivilegiada" 
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" VIS
 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"  AS INDE on (INDE."IdUso"='IND' AND INDE."CodigoTipoIndicador"='EMPBANCO' AND  vis."IdEmpleado" = INDE."IdEmpleado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"  AS INDFEC on (INDFEC."IdUso"='IND' AND INDFEC."CodigoTipoIndicador"='FEC_ACR' AND  vis."IdEmpleado" = INDFEC."IdEmpleado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado"  AS INDPRIV on (INDPRIV."IdUso"='IND' AND INDPRIV."CodigoTipoIndicador"='ACC_INFP' AND  vis."IdEmpleado" = INDPRIV."IdEmpleado");

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 