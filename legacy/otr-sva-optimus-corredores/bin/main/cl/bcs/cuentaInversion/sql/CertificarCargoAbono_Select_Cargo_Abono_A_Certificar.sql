

	SELECT 
		0 as "IdSesion" ,
		CBM."IdCargoAbonoMovimiento"  ,
		CBM."IdCondicionDeLiquidacion" ,
		CBM."IdCuentaInversion" ,
		CBM."FechaEfectivaMovimiento" ,
		CBM."FechaSistema" ,
		CBM."FechaIngresoRegistro" ,
		CBM."IdTipoMovimiento" ,
		CBM."EsCargoAbonoPorTraspaso" ,
		CBM."IdCargoAbonoRelacionadoTraspaso" ,
		CBM."Monto" ,
		CBM."IdEstado" ,
		CI."IdPortafolio" ,
		CI."IdMoneda" ,
		COALESCE(CL."PlazoEnDias", 0)  ,
		'0001-01-01' as "FechaCalculada"  ,
		'S' as "Informado" ,
		
	
	    CLI."IdCliente" AS "IdSecuencia",
	    PORT."CodigoPortafolio",      
	    CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
	    PER."IdPersona",
	    IDENT."DocumentoIdentidad",
	    CLI."IdTipoCliente",     
	    DI."IdTipoDocumentoIdentidad" AS "TipoDocumento"  ,
	    PER."IdDireccionElectronicaPrincipal" ,
	    CPT."Numero" AS "TelCliente" ,
	    TM."Nombre" AS "NombreOperacion" ,
	    CCI."NombreConcepto" AS "NombreConcepto" ,
	    
 

       DIRELE."Direccion" AS "Correo" , 
       
       
	    CASE PEREMP."Tipo" 
			WHEN 'N' THEN PEREMP."Nombres" ||' '|| PEREMP."ApellidoPaterno" || ' ' || PEREMP."ApellidoMaterno"			
			ELSE PEREMP."RazonSocial"
		END  as "Agente"
  
		
	FROM "BSCL.CuentaInversion.BD::CTA.CertificarCargoAbono_IN_CargosAbonosACertificar" AS CBC

	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CBM
		    ON CBM."IdCargoAbonoMovimiento" = CBC."IdCargoAbonoMovimiento"
		    AND CBM."IdEmpresa" = :p_IdEmpresa

   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = CBM."IdCuentaInversion"
   LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CL  ON CL."IdCondicionDeLiquidacion" = CBM."IdCondicionDeLiquidacion"
    

   
   INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT  ON (PORT."IdPortafolio" = CI."IdPortafolio")
   INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente" = PORT."IdCliente")
   INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER  ON (CLI."IdPersona" = PER."IdPersona")
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad") 
   

   LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" CPT  ON CPT."IdTelefono" = PER."IdTelefonoPrincipal"
   
   
   INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI  ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
   INNER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" TM  ON TM."IdTipoMovimiento" = CBM."IdTipoMovimiento"
   

   
  

   LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRELE ON (DIRELE."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal" AND DIRELE."Borrado" = 'N')
   
   
   INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" CCI  ON CCI."IdConcepto" = CBM."IdConcepto"    
   LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON (EMP."IdEmpleado" = PORT."IdEmpleadoEjecutivo")
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS PEREMP ON (PEREMP."IdPersona" = EMP."IdPersona")
     
   
   
   UNION ALL
       
       
       
   SELECT 
	   0 as "IdSesion" ,
	   CBM."IdCargoAbonoMovimiento"  ,
	   CBM."IdCondicionDeLiquidacion" ,
	   CBM."IdCuentaInversion" ,
	   CBM."FechaEfectivaMovimiento" ,
	   CBM."FechaSistema" ,
	   CBM."FechaIngresoRegistro" ,
	   CBM."IdTipoMovimiento" ,
	   CBM."EsCargoAbonoPorTraspaso" ,
	   CBM."IdCargoAbonoRelacionadoTraspaso" ,
	   CBM."Monto" ,
	   CBM."IdEstado" ,
	   CI."IdPortafolio" ,
	   CI."IdMoneda" ,
	   COALESCE(CL."PlazoEnDias", 0) ,
	   '0001-01-01' as "FechaCalculada" ,
	   'N' as "Informado" ,  
	   
  
         
       CLI."IdCliente" AS "IdSecuencia"  , 
       PORT."CodigoPortafolio",            
        CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
       PER."IdPersona" , 
       IDENT."DocumentoIdentidad",
       CLI."IdTipoCliente" ,        
       DI."IdTipoDocumentoIdentidad" AS "TipoDocumento"  ,
       PER."IdDireccionElectronicaPrincipal" ,
       CPT."Numero" AS "TelCliente" ,
       TM."Nombre" AS "NombreOperacion" ,
       CCI."NombreConcepto" AS "NombreConcepto" ,
       

       DIRELE."Direccion" AS "Correo" ,      
      
        
       CASE PEREMP."Tipo" 
			WHEN 'N' THEN PEREMP."Nombres" ||' '|| PEREMP."ApellidoPaterno" || ' ' || PEREMP."ApellidoMaterno"			
			ELSE PEREMP."RazonSocial"
	   END  as "Agente" 

    
    FROM "BSCL.CuentaInversion.BD::CTA.CertificarCargoAbono_IN_CargosAbonosACertificar" AS CBC
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CBM
	    ON  CBM."IdCargoAbonoRelacionadoTraspaso" = CBC."IdCargoAbonoMovimiento"
	    AND CBM."IdEmpresa" = :p_IdEmpresa
    
	    
	 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI
    ON CI."IdCuentaInversion" = CBM."IdCuentaInversion"
    
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CL 
	    ON CL."IdCondicionDeLiquidacion" = CBM."IdCondicionDeLiquidacion"
    
    


   INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" PORT  ON (PORT."IdPortafolio" = CI."IdPortafolio")
   INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente" = PORT."IdCliente")
   INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER  ON (CLI."IdPersona" = PER."IdPersona")
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
   

   LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" CPT  ON CPT."IdTelefono" = PER."IdTelefonoPrincipal"
   
   INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI  ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
   INNER JOIN "BSCL.Plataforma.BD::PLA.TipoMovimiento" TM  ON TM."IdTipoMovimiento" = CBM."IdTipoMovimiento"
   

  
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRELE ON (DIRELE."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal" AND DIRELE."Borrado" = 'N')
  
   
   INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" CCI  ON CCI."IdConcepto" = CBM."IdConcepto"        
   LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON (EMP."IdEmpleado" = PORT."IdEmpleadoEjecutivo")
   LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona"  AS PEREMP ON (PEREMP."IdPersona" = EMP."IdPersona");
       

       