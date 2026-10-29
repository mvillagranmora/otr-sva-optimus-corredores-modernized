SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarCliente";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarCliente"(
"IdPersona"
,"IdCliente"
,"IdEstadoCliente"
,"DocumentoIdentidad" -- por tipo rut
,"IdTipoDocumentoIdentidad"
,"Nombres"
,"ApellidoPaterno"
,"ApellidoMaterno"
,"RazonSocial"
,"Giro"
,"PaisNacimiento"
,"PaisResidencia"
,"FechaNacimiento"
,"FechaDefuncion"
,"Sexo"
,"EsExtranjero"
,"EstadoCivil"
,"RegimenConyugal"
,"Cargo"
,"Direccion"
,"Calle"
,"NumeroDirf"
,"Departamento"
,"NumeroTelefono"
,"Profesion"
,"CodigoPortafolio"
,"Proposito"
,"IdEstadoPortafolio"
 ) AS 
 
select 
per."IdPersona"
,cli."IdCliente"
,cli."IdEstado"
,doc."DocumentoIdentidad" -- por tipo rut
,doc."IdTipoDocumentoIdentidad"
,per."Nombres"
,per."ApellidoPaterno"
,per."ApellidoMaterno"
,per."RazonSocial"
,Giro."Nombre"
,PaisNac."NombrePais"
,PaisRes."NombrePais"
,per."FechaNacimiento"
,per."FechaDefuncion"
,per."Sexo"
,per."EsExtranjero"
,per."EstadoCivil"
,per."RegimenConyugal"
,per."Cargo"
,dire."Direccion"
,dirf."Calle"
,dirf."Numero"
,dirf."Departamento"
,tel."Numero"
,Prof."Nombre"
,por."CodigoPortafolio"
,por."Proposito"
,por."IdEstado"
					
FROM      
		"BSCL.SocioNegocio.BD::PER.Persona" as per		
        left join "BSCL.Plataforma.BD::REG.Giro"  as Giro on (per."IdGiroPrincipal" = Giro."IdGiro")
        left join "BSCL.Plataforma.BD::REG.Pais"  as PaisNac on (per."IdPaisNacimiento" = PaisNac."IdPais")
        left join "BSCL.Plataforma.BD::REG.Pais"  as PaisRes on (per."IdPaisResidencia" = PaisRes."IdPais")
        left join "BSCL.Plataforma.BD::REG.Profesion"  as Prof on (per."IdProfesion" = Prof."IdProfesion")
        ---------------- CLIENTE
  	    left join "BSCL.SocioNegocio.BD::CLI.Cliente" as Cli on per."IdPersona" = cli."IdPersona" 
		---------------- Documento Identidad
		left join "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"     as doc on (per."IdPersona" = doc."IdPersona" and doc."IdTipoDocumentoIdentidad" = 'R')		
        ---------------- Direccion Electronica
  	    left join "BSCL.SocioNegocio.BD::PER.DireccionElectronica"   as dire on (per."IdDireccionElectronicaPrincipal" = dire."IdDireccionElectronica"  and per."IdPersona" = dire."IdPersona")
        ---------------- Direccion Fisica
  	    left join "BSCL.SocioNegocio.BD::PER.DireccionFisica" as dirf on (per."IdDireccionFisicaPrincipal" = dirf."IdDireccionFisica" and per."IdPersona" = dirf."IdPersona")
        ---------------- Telefono
  	    left join "BSCL.SocioNegocio.BD::PER.Telefono" as tel on (per."IdTelefonoPrincipal" = tel."IdTelefono" and per."IdPersona" = tel."IdPersona")  	 	 				    
  	    ---------------- Portafolio
		left join "BSCL.Portafolio.BD::POR.Portafolio" as por on (cli."IdCliente" = POR."IdCliente")

order by per."IdPersona"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD::NOR.ConsultarCliente"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;