SET SCHEMA "BSCL";
 
ALTER TABLE "BSCL.Plataforma.BD::PLA.Moneda"
 ADD ("CodigoBancoCentral" NVARCHAR(40) NOT NULL DEFAULT '900',
      "DescripcionBancoCentral" NVARCHAR(100) NOT NULL DEFAULT 'OTRAS NO ESPECIFICADAS');

ALTER TABLE "BSCL.Facturacion.BD::FAC.TareaDTE"
 ADD ("CodigoMonedaBancoCentral" NVARCHAR(40) NOT NULL DEFAULT '900',
      "NemoMoneda" NVARCHAR(100) NOT NULL DEFAULT '');

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='5', "DescripcionBancoCentral"='REAL' WHERE "IdMoneda"='BRL';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='6', "DescripcionBancoCentral"='DÓLAR CANADIENSE' WHERE "IdMoneda"='CAD';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='13', "DescripcionBancoCentral"='DÓLAR DE ESTADOS UNIDOS' WHERE "IdMoneda"='USD';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='24', "DescripcionBancoCentral"='NUEVO SOL' WHERE "IdMoneda"='PEN';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='36', "DescripcionBancoCentral"='DÓLAR AUSTRALIANO' WHERE "IdMoneda"='AUD';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='48', "DescripcionBancoCentral"='YUAN' WHERE "IdMoneda"='RMB';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='51', "DescripcionBancoCentral"='CORONA DANESA' WHERE "IdMoneda"='DKK';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='72', "DescripcionBancoCentral"='YEN' WHERE "IdMoneda"='JPY';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='82', "DescripcionBancoCentral"='FRANCO SUIZO' WHERE "IdMoneda"='CHF';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='96', "DescripcionBancoCentral"='CORONA NORUEGA' WHERE "IdMoneda"='NOK';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='97', "DescripcionBancoCentral"='DÓLAR DE NUEVA ZELANDA' WHERE "IdMoneda"='NZD';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='102', "DescripcionBancoCentral"='LIBRA ESTERLINA' WHERE "IdMoneda"='GBP';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='113', "DescripcionBancoCentral"='CORONA SUECA' WHERE "IdMoneda"='SEK';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='127', "DescripcionBancoCentral"='DÓLAR HONG KONG' WHERE "IdMoneda"='HKD';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='132', "DescripcionBancoCentral"='PESO MEXICANO' WHERE "IdMoneda"='MXN';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='142', "DescripcionBancoCentral"='EURO' WHERE "IdMoneda"='EUR';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='999', "DescripcionBancoCentral"='PESO CHILENO' WHERE "IdMoneda"='CLP';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='900', "DescripcionBancoCentral"='OTRAS NO ESPECIFICADAS' WHERE "IdMoneda"='COP';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='900', "DescripcionBancoCentral"='OTRAS NO ESPECIFICADAS' WHERE "IdMoneda"='CUP';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='900', "DescripcionBancoCentral"='OTRAS NO ESPECIFICADAS' WHERE "IdMoneda"='UF';

UPDATE "BSCL.Plataforma.BD::PLA.Moneda"
SET "CodigoBancoCentral"='900', "DescripcionBancoCentral"='OTRAS NO ESPECIFICADAS' WHERE "IdMoneda"='DO';

ALTER PROCEDURE "BSCL.Facturacion.BD.Servicios::DteEnviarComprobante"
  (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3), 
     
     IN p_simularOperacion    NVARCHAR(1) ,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio    NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N', -- S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente     
   
     -- Parametros de Proceso 
     IN p_ComprobanteAEnviar table("IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdComprobante" BIGINT, "IdTipoComprobante" NVARCHAR(3)) DEFAULT EMPTY,
     
     --Salidas para mensajes de advertencias y errores
     OUT p_out_InformacionContador  	 INT,
     OUT p_out_InformacionDetalle   	 table ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),   
     OUT p_out_ResultadoProceso		     table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)) DEFAULT EMPTY,
     OUT p_out_AdvertenciasContador      int,
     OUT p_out_AdvertenciasDetalle       table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
     OUT p_out_ErroresContador           int,   
     OUT p_out_ErroresDetalle            table ("IdComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY                              
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
	--READS SQL DATA
AS
BEGIN
    --------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("IdComprobante" BIGINT, "Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520)); 	
    DECLARE v_RechazadosPorValidacion	TABLE("IdComprobante" BIGINT)
            Default Select 0 As "IdComprobante" From dummy where 1=2;
	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN		
		v_hash= Select 0 As "IdInterno", '' As "Codigo", '' As "CodigoDato1", '' As "ValorDato1", '' As "CodigoDato2",
                '' As "ValorDato2" From Dummy;	
        v_out_hash = Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
                
        Call "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo"(:v_hash, :v_out_hash);
        
		p_out_ErroresDetalle = Select "IdInterno" As "IdComprobante", "Codigo"  as "Codigo",
		                              "Descripcion" As "Descripcion" 
		                       From :v_out_hash Union All
		                       Select * From :p_out_ErroresDetalle; 
								
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	
	
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT 0 As "IdComprobante", v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log serï¿½ captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	
	p_out_AdvertenciasContador := 0;
	p_out_ErroresContador		:= 0;
	
	
	Begin --Logica
       --------------------
	   --Declare Variables
	   --------------------
       DECLARE v_IdTareaDTEGrupo, v_out_AdvertenciasContador, v_out_ErroresContador, Cant_Cmte, Cant_ComprobanteEnviado  BIGINT := 0;
       
       DECLARE v_IndicadorPresenciaBursatil  BIGINT;
       
       DECLARE v_GlosaFacturaOtrosConceptosPeriodo NVARCHAR(2);
             
       DECLARE p_TablaParametros TABLE ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100)) 
               Default Select '' As "NombreParametro", '' As "Valor" from dummy where 1=2;
       
       DECLARE v_IdPersona BIGINT;
      
       DECLARE v_EmailReceptor NVARCHAR(4000);
      
       DECLARE v_EmailEmisor NVARCHAR(4000);
      
       DECLARE v_IdDireccionElectronicaReceptor NVARCHAR(4000);
      
       DECLARE v_EmailReceptorFinal NVARCHAR(4000);
      
       DECLARE v_Emails TABLE ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
      
       DECLARE v_Contador BIGINT := 0;
       
	   -----------------------------------------------------
	   --Validaciones
	   -----------------------------------------------------
	   v_IndicadorPresenciaBursatil := 25;
   
	   SELECT "Valor" INTO v_IndicadorPresenciaBursatil  
	   FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
	   WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" =  'IndicadorPresenciaBursatil';	
	   
	   ----Condiciona segun indicador glosa para facturas FOE   
	   SELECT "Valor" 
	   INTO v_GlosaFacturaOtrosConceptosPeriodo
	   FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
	   WHERE "IdEmpresa" = :p_IdEmpresa
	     AND "CodigoParametro" = 'GLOSAFACTURAOTROSCONCEPTOSPERIODO';
               
       If p_UsaReglasNegocio = 'S' Then
		 BEGIN
            If p_EsIngresoAutomatico = 'S' Then
			   Call "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalleReqBas,:v_resultadoRegla);
			   If :v_resultadoRegla = 'RECH' Then
				  SIGNAL SALIDA_CONTROLADA; 	
			   End If;
			   
            End If;
            
            --Validacion Facturaciï¿½n
            Call "BSCL.Facturacion.BD.Reglas::ServicioCumpleRequisitosDteEnviarComprobante"(:p_ComprobanteAEnviar,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		    If :v_resultadoRegla = 'RECH' Then	
		        p_out_ResultadoProceso = Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		                                        'BSCL.Facturacion.BD.Servicios::AnularComprobante' as "Servicio",
		                                        'RECH' as "Resultado"
		                                 From :v_RechazadosPorValidacion;
			  			
			    p_out_ErroresDetalle = Select "IdComprobante", "Codigo","Descripcion" 
			                           From :v_ErroresDetalle;			                           	
                
			    SIGNAL SALIDA_CONTROLADA; 	
		    Else
		      --If :v_resultadoRegla = 'ADVE' Then
			     --LLamada al servicio de autorizacion si se requiere
			     p_out_AdvertenciasDetalle = Select "IdComprobante", "Codigo",'N' as "RequiereAutorizacion", "Descripcion" 
			                                 From :v_ErroresDetalle;
			                                 
			     Select count(1) 
			     Into p_out_AdvertenciasContador 
			     From :p_out_AdvertenciasDetalle;
		      --End If;   
		    End If;  
          END;  --End Validaciones
        End If;
        
        --Simulaciï¿½n
        If (:p_simularOperacion = 'S') Then
		   SIGNAL SALIDA_CONTROLADA;
	    End If;
	    	    
	    --------------------
		--Negocio Aqui
		--------------------
		/** Inicio Preparacion Envio a DTE **/    
        --Obtiene Id Grupo DTE
        

	    Select "BSCL.Facturacion.BD.Secuencias::TareaDTEGrupo".Nextval
        Into v_IdTareaDTEGrupo 
        From dummy;
        
        v_ComprobanteAEnviar = Select "IdPortafolio", "IdCliente", "IdComprobante", "IdTipoComprobante",
                                      :v_IdTareaDTEGrupo As "IdTareaDTEGrupo"       
                               From :p_ComprobanteAEnviar As CmbteEnv  
                               Where CmbteEnv."IdComprobante" Not In (Select "IdComprobante" 
                                                                      From :v_RechazadosPorValidacion);
        
        
        Select Count("IdComprobante")
        Into Cant_Cmte
        From :v_ComprobanteAEnviar;
        
        If Cant_Cmte <=0 Then
           p_out_ErroresContador := p_out_ErroresContador + 1;
		    
		   p_out_ErroresDetalle = Select 0 As "IdComprobante", '' As "Codigo", 
		                                 'Los comprobantes enviados presentan problemas' As "Descripcion" 
		                          From dummy Union All		                          
	                              Select "IdComprobante", "Codigo", "Descripcion" 
	                              From :p_out_ErroresDetalle;
	                              
           SIGNAL SALIDA_CONTROLADA;
        End If;
        
         

        /** Inicio Actualizacion estados en facturacion **/  
        --Actualiza estado enviado a Movimiento Comprobante Detalle
        Update MvtoCmbteDet
        Set "IdEstadoMovimientoComprobanteDetalle" = 'ENV'
        From "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte          
          Inner Join :v_ComprobanteAEnviar CmbteEnv
             On CmbteEnv."IdComprobante" =  Cmbte."IdComprobante"
            And CmbteEnv."IdCliente" =  Cmbte."IdCliente"
            And CmbteEnv."IdPortafolio" =  Cmbte."IdPortafolio"
            And CmbteEnv."IdTipoComprobante" =  Cmbte."IdTipoComprobante"
          Inner Join "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CmbteDet
             On CmbteDet."IdComprobante" = Cmbte."IdComprobante"
          Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MvtoCmbteDet
             On MvtoCmbteDet."IdComprobanteDetalle" = CmbteDet."IdComprobanteDetalle"; 
             
        
        --Actualiza estado enviado a Comprobante Detalle
        Update CmbteDet
        Set "IdEstadoComprobanteDetalle" = 'ENV'
        From "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte          
          Inner Join :v_ComprobanteAEnviar CmbteEnv
             On CmbteEnv."IdComprobante" =  Cmbte."IdComprobante"
            And CmbteEnv."IdCliente" =  Cmbte."IdCliente"
            And CmbteEnv."IdPortafolio" =  Cmbte."IdPortafolio"
            And CmbteEnv."IdTipoComprobante" =  Cmbte."IdTipoComprobante"
          Inner Join "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CmbteDet
             On CmbteEnv."IdComprobante" =  CmbteDet."IdComprobante";      
                       
                
        --Actualiza estado enviado a Comprobante
        Update Cmbte
        Set "IdEstadoComprobante" = 'ENV',
            "IdEstadoDTE" = 'ENV'
        From "BSCL.Facturacion.BD::FAC.Comprobante" Cmbte
          Inner Join :v_ComprobanteAEnviar CmbteEnv
             On CmbteEnv."IdComprobante" =  Cmbte."IdComprobante"
            And CmbteEnv."IdCliente" =  Cmbte."IdCliente"
            And CmbteEnv."IdPortafolio" =  Cmbte."IdPortafolio"
            And CmbteEnv."IdTipoComprobante" =  Cmbte."IdTipoComprobante";
        
        /** Fin Actualizacion estados en facturacion **/  
        
        
        
        /** Inicio Preparacion Envio a DTE **/    

        --Filtros parametros para servicio agregar programa
        p_TablaParametros = Select 'IdTareaDTEGrupo' As "NombreParametro", Cast("IdTareaDTEGrupo" As nvarchar(100)) As "Valor" 
                            From :v_ComprobanteAEnviar; 
                            
                            
                            
        --Tabla Temporal para usar en Job
        --Obtiene montos para job  
        v_CmbteMontoADTE = Select ConsCmbte."IdComprobante", ConsCmbte."IdTipoComprobante", 
                              CASE
                                WHEN Max(ConsCmbte."MontoComprobante") > 0 AND ConsCmbte."IdTipoComprobante" NOT IN ('FEE', 'NEE', 'FOE', 'NEO', 'FOX') THEN Max(ConsCmbte."MontoComprobante")
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FEE', 'NEE', 'FOE', 'NEO', 'FOX') THEN 0
                              ELSE Max(ConsCmbte."MontoLiquidacion")
                              END As "MontoNoFacturable",
                              CASE
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FEE', 'NEE', 'FOX') THEN Max(ConsCmbte."MontoLiquidacion")
                              ELSE 0
                              END As "MontoExento",                                  
                              CASE
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FEE', 'NEE','FOX') THEN 0
                              ELSE 19
                              END As "TasaIva",
                              CASE
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FEE', 'NEE', 'NCE','FOE', 'NEO', 'FOX') AND Coalesce(Max(ConsCmbteCobIva."MontoLocalCobro"),0) = 0 THEN Max(ConsCmbte."MontoLiquidacion")
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FOE', 'NEO') AND Coalesce(Max(ConsCmbteCobIva."MontoLocalCobro"),0) > 0 THEN Coalesce(Max(ConsCmbte."MontoLiquidacion"),0) + Coalesce(Max(ConsCmbteCobIva."MontoLocalCobro"), 0)
                              ELSE Coalesce(Sum(ConsCmbteCobSinIva."MontoLocalCobro"), 0) + Coalesce(Max(ConsCmbteCobIva."MontoLocalCobro"), 0) 
                              END As "MontoTotal"
                           From "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
                              Inner Join :v_ComprobanteAEnviar As CmbteAEnviar
                                 On CmbteAEnviar."IdComprobante" = ConsCmbte."IdComprobante"
                              Left join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" As ConsCmbteCobIva
                                 On ConsCmbteCobIva."IdComprobante" = ConsCmbte."IdComprobante"
                                And ConsCmbteCobIva."IdCobro" = 'IVA'
                              Left join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" As ConsCmbteCobSinIva
                                 On ConsCmbteCobSinIva."IdComprobante" = ConsCmbte."IdComprobante"   
                                And ConsCmbteCobSinIva."IdCobro" <> 'IVA'
                           Group By ConsCmbte."IdComprobante", ConsCmbte."IdTipoComprobante";
                           
                           
        /*select 'v_CmbteMontoADTE', *                   
        from :v_CmbteMontoADTE;*/
         v_DirElectronica = SELECT Cli."IdCliente", STRING_AGG(DirElec."Direccion", '|') AS "Direccion"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS Cli
				Inner Join :v_ComprobanteAEnviar As CmbteAEnviar
				On CmbteAEnviar."IdCliente" = Cli."IdCliente"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS GrupDir
				ON GrupDir."IdPersona" = Cli."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle" AS GrupDirDe
				ON GrupDir."IdGrupoDireccion" = GrupDirDe."IdGrupoDireccion"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DirElec
				ON GrupDirDe."IdDireccionElectronica" = DirElec."IdDireccionElectronica"
				WHERE GrupDir."Borrado" = 'N' AND GrupDir."IdEstado" = 'ACT' AND DirElec."IdEstado" = 'ACT'
				group by Cli."IdCliente";
			
		
						                   
        v_CmbteADTE = Select "BSCL.Facturacion.BD.Secuencias::TareaDTE".Nextval	As "IdTareaDTE", 
                             CmbteAEnviar. "IdTareaDTEGrupo",
                             'DTEENV' As "TipoTareaDTE",
                             0 As "IdRespuestaDTE",
                             '' As "RespuestaDTEEnvioSII",
                             '' As "RespuestaLocationDTE",
                             '' As "RespuestaPdfDTE",
                             '' As "RespuestaXmlDTE",
                             0 As "FolioComprobanteRespuestaDTE",
                             ConsCmbte."IdComprobante",
                             'PEN' As "IdEstadoTareaDTE",
                             '' As "IdEstadoDTE",
                             '' As "IdEstadoSII",
                             ConsCmbteDet."IdTipoComprobante" As "IdTipoComprobante",
                             TipCmbte."TipoComprobanteDTE",                             
                             ConsCmbte."FolioComprobante" As "FolioComprobante",
                             ConsCmbteDet."AccionTipoOperacion" As "AccionTipoOperacion",                             
                             ConsCmbteDet."FechaComprobante",
                             ConsCmbteDet."FechaLiquidacion" As "FechaVencimiento",                   
                             Case
                               When ConsCmbteDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') Then ''
                               When ConsCmbte."IdConcepto" = 'COMMEN' Then '' 
                               When ConsCmbteDet."IdTipoOperacion" IN ('COMSPT','VENSPT') Then
                               	(Case  when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA'  THEN 'xxxCONTRA'
                              		 when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'xxxFAVOR'
                              		 when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'xxxCONTRA'
                               	End)
                               When ConsCmbte."IdClasificacionTipoInstrumento" = 'RV' THEN (Case                                                       
                                                                                              When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA'  THEN 'xxxCONTRA'
                                                                                              When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'xxxFAVOR'
                                                                                              When ((length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0) OR (length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0)) AND ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'xxxCONTRA'
                                                                                              When ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA'  THEN 'xxxCONTRA'
                                                                                              When ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'xxxFAVOR'
                                                                                              When ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'xxxCONTRA'
                                                                                            END
                                                                                           )
                               when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'COMPRA'  THEN 'xxxCONTRA'
                               when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) > 0)  THEN 'xxxFAVOR'
                               when ConsCmbteDet."IdTipoComprobante" In ('NDE','FAC', 'FAE', 'FOE','FOC', 'BOE', 'FEE', 'FOX') And ConsTipOpe."Accion" = 'VENTA' And (ConsCmbte."MontoLiquidacion" - Coalesce(ConsCmbte."MontoCobro", 0) < 0) Then 'xxxCONTRA'  
                             Else ''
                             End As "CodigoReceptor", 
                         
                             Coalesce(CmbteMonADTE."MontoNoFacturable",0) As "IdComprobanteMontoNoFacturable",                             
                             
                             Coalesce(CmbteMonADTE."MontoExento",0) As "IdComprobanteMontoExento",
                             CmbteMonADTE."TasaIva" As "IdComprobanteTasaIva",
                             Coalesce(CmbteMonADTE."MontoTotal",0) As "IdComprobanteMontoTotal",
                             CASE 
                               WHEN ConsCmbte."IdTipoInstrumento" = 'CFI' AND ConsCmbte."IdMonedaLiquidacion" <> 'CLP' THEN COALESCE(ABS(ConsCmbte."MontoComprobanteFinal"), 0)
                               WHEN ConsCmbteDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') Then 0
                               WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND UPPER(ConsCmbteDet."AccionTipoOperacion") = UPPER('VENTA') then Coalesce(ConsCmbte."MontoComprobanteFinal",0)
	                       WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND UPPER(ConsCmbteDet."AccionTipoOperacion") = UPPER('COMPRA') then (Coalesce(ConsCmbte."MontoComprobanteFinal",0) * -1)
                               WHEN ConsCmbte."MontoLiquidacionFinal" < 0 THEN (Coalesce(ConsCmbte."MontoLiquidacionFinal",0) * -1)
                               WHEN ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' then Coalesce(ConsCmbte."MontoComprobanteFinal",0)
                               ELSE Coalesce(ConsCmbte."MontoLiquidacionFinal",0)
                             END As "IdComprobanteValorAPagar",
                             
                            /** Corredora **/ 
                             IDENT."DocumentoIdentidad" As "DocumentoIdentidadEmisor",
                             Case
                               When Length(ConsPer."RazonSocial") > 0 Then ConsPer."RazonSocial"
                             Else ''
                             End As "RazonSocialEmisor", 
                             Case 
                               When Length(GiroEmisor."Nombre") > 0 Then GiroEmisor."Nombre"
                             Else 'En definicion'
                             End As "GiroEmisor",
                             Case
                                When Length(FIS."Calle" || ' ' || FIS."Numero") > 0 Then (FIS."Calle" || ' ' || FIS."Numero")
                             Else 'En definicion'
                             End AS "DireccionEmisor",
                             Case
                                When Length(DIV."NombreDivisionPais") > 0 Then DIV."NombreDivisionPais"
                             Else 'En definicion'
                             End AS "ComunaEmisor",                             
                             Case
                                When Length(CIU."NombreCiudad") > 0 Then CIU."NombreCiudad"
                             Else 'En definicion'
                             End AS "CiudadEmisor",                             
                             CASE
                                WHEN Length(ELE."Direccion") > 0 Then ELE."Direccion"
                             Else 'En definicion'
                             End AS "EmailEmisor",
                             CondLiq."Codigo" AS "CondicionPago",
                             TEL."Numero" As "TelefonoEmisor",                              
                             /** Cliente **/
                             CASE
                               WHEN IndEnvMail."ValorIndicador" = 'N' AND Length(ELE."Direccion") > 0 THEN ELE."Direccion"
                             ELSE COALESCE(EmailRespaldo."Direccion", DirElec."Direccion")
                             END AS "EmailReceptor",
                            COALESCE("IDENTCLI"."DocumentoIdentidad", "IDENTCLI"."DocumentoIdentidad",'') As "DocumentoIdentidadReceptor",                           
                              Portafolio."CodigoPortafolio" AS "CodigoSucursal",                             
                             ConsCmbte."NombreLargoCliente" As "RazonSocialReceptor",
                             Case 
                               When Length(GiroReceptor."Nombre") > 0 Then GiroReceptor."Nombre"
                             Else 'En definicion'
                             End As "GiroReceptor",
                             Case
                               When Length(DirFis."Calle" || ' ' || DirFis."Numero" || ' ' || DirFis."Departamento") > 0 Then (DirFis."Calle" || ' ' || DirFis."Numero" || ' ' || DirFis."Departamento")  
                             Else 'En definicion' 
                             End As "DireccionReceptor",
                              Case
                                When Length(ConsDivPais."NombreDivisionPais") > 0 Then ConsDivPais."NombreDivisionPais"
                              Else 'En definicion'
                              End As "ComunaReceptor",                              
                              Case 
                                When Length(Ciudad."NombreCiudad") > 0 Then Ciudad."NombreCiudad"
                              Else 'En definicion'
                              End As "CiudadReceptor", 
                              CondLiq."Codigo" AS "ContactoReceptor",                              
                              ConsCmbte."IdBolsa" As "IndBolsa",
                              --AjusteDocumento
                              'COMISION' As "GlosaAjusteComision",
                              IFNULL(ConsCmbte."TotalComisCmbte",0) As "TotalComisionCmbte",
                              'IMPUESTO' As "GlosaAjusteImpuesto",
                              IFNULL(ConsCmbte."TotalImptoCmbte",0) As "TotalImpuestoCmbte",
                              'DERECHO' As "GlosaAjusteDerecho",
                              IFNULL(ConsCmbte."TotalDereCmbte",0) As "TotalDerechoCmbte",
                              'GASTO' As "GlosaAjusteGasto",
                              IFNULL(ConsCmbte."TotalGastoCmbte",0) As "TotalGastoCmbte",
                              'RETENCION' As "GlosaAjusteRetencion",
                              IFNULL(ConsCmbte."TotalRetencionCmbte",0) As "TotalRetencionCmbte",                              
                              --ReferenciaDocumento
                              ---
                              'OPT' As "TipoReferenciaIdComprobante",                              
                              ConsCmbte."IdComprobante" As "FolioReferenciaIdComprobante",  
                              Cast(ConsCmbte."FechaComprobante" as nvarchar(10)) As "FechaReferenciaIdComprobante",
                              'Nro. Interno Optimus SAP' As "RazonReferenciaIdComprobante",
                              ---
                              'VIS' As "TipoReferenciaFolioComprobante", 
                              ConsCmbte."FolioComprobante" As "FolioReferenciaFolioComprobante",  
                              '' As "FechaReferenciaFolioComprobante",
                              'Nro. Visible SAP' As "RazonReferenciaFolioComprobante", 
                              ---Notas Debito / Credito
                              CASE 
                                WHEN (ConsCmbteDet."IdTipoComprobante" = 'NDE' OR ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO' OR ConsCmbteDet."IdTipoComprobante" = 'NEE') THEN 1 
                              ELSE 0
                              END AS "NumeroReferenciaNota",
                              CASE 
                                WHEN (ConsCmbteDet."IdTipoComprobante" = 'NDE' OR ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO' OR ConsCmbteDet."IdTipoComprobante" = 'NEE') THEN Cast(TipCmbteNota."TipoComprobanteDTE" AS Nvarchar(10)) 
                              ELSE ''
                              END As "TipoReferenciaNota",
                              CASE 
                                WHEN (ConsCmbteDet."IdTipoComprobante" = 'NDE' OR ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO' OR ConsCmbteDet."IdTipoComprobante" = 'NEE') THEN ConsCmbteDetNota."FolioComprobante" 
                              ELSE 0
                              END As "FolioReferenciaNota",
                              CASE 
                                WHEN (ConsCmbteDet."IdTipoComprobante" = 'NDE' OR ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO' OR ConsCmbteDet."IdTipoComprobante" = 'NEE') THEN ConsCmbteDetNota."FechaComprobante" 
                              ELSE ''
                              END As "FechaReferenciaNota",  --- jvillagran .. revisar .......
                              
                              '' AS "RazonReferenciaNota",
                              

                              CASE 
                                
                                WHEN (ConsCmbteDet."IdTipoComprobante" = 'NDE' OR ConsCmbteDet."IdTipoComprobante" = 'NCR' OR ConsCmbteDet."IdTipoComprobante" = 'NCE' OR ConsCmbteDet."IdTipoComprobante" = 'NEO' OR ConsCmbteDet."IdTipoComprobante" = 'NEE') THEN '1'
                              ELSE 0
                              END As "CodigoReferenciaNota",
                              --DetalleDocumento                              
                              CASE 
                                 WHEN ConsCmbte."IdTipoComprobante" IN ('FAE', 'FOE', 'FOX', 'FAC', 'FEE') THEN CASE
                                                                                                                   WHEN ConsCmbte."IdConcepto" = 'CMMNRV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN ConsCmbte."Glosa"
                                                                                                                   WHEN ConsCmbte."IdConcepto" = 'CMMNRV' THEN 'COMISIÃ“N TRIMESTRAL NO APV - Acciones, CFI, ETFs - Trimestre'
                                                                                                                   WHEN ConsCmbte."IdConcepto" = 'CMTAPV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN ConsCmbte."Glosa"
                                                                                                                   WHEN ConsCmbte."IdConcepto" = 'CMTAPV' THEN 'COMISIÃ“N TRIMESTRAL APV - Acciones, CFI, ETFs - Trimestre'
                                                                                                                   WHEN ConsCmbte."IdConcepto" = 'SUECOS' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsTipOpe."Nombre" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' 
                                                                                                                   || CASE 
                                                                                                                        WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                      ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                      END || 'por AcciÃ¯Â¿Â½n.' || CASE
                                                                                                                                                  WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                  WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                ELSE ''
                                                                                                                                                END
                                                                                                                   WHEN ConsCmbte."IdConcepto" IN ('VNOSAC', 'CMOSAC')  THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades ' || 'a un valor de ' 
                                                                                                                   || CASE 
                                                                                                                        WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                      ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                      END || 'por AcciÃ¯Â¿Â½n.' || CASE
                                                                                                                                                  WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                  WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                ELSE ''
                                                                                                                                                END
                                                                                                                   WHEN ConsCmbte."IdTipoInstrumento" = 'ACN' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por AcciÃ³n.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                                   WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                                   WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                                ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                                END
                                                                                                                   WHEN ConsCmbte."IdTipoInstrumento" = 'CFI' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por CFI.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                             ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                             END
                                                                                                                   WHEN ConsCmbte."IdTipoInstrumento" = 'ETN' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por ETF.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                             ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                             END
                                                                                                                   WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN CASE 
                                                                                                                                                                     WHEN ConsCmbteDet."AccionTipoOperacion" = 'COMPRA' THEN 'FX-COMPRA '
                                                                                                                                                                   ELSE 'FX-VENTA  '
                                                                                                                  						   END || CASE 
																                        		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																                        			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
																                        		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" <> OpSpt."IdComprobanteFacturacion" 
																                        			THEN OpSpt."IdMonedaSecundaria" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaSecundaria" || '/' || OpSpt."IdMonedaPrincipal"
																                        		WHEN MovCom."NemotecnicoInstrumento" IN ('DISTRI','INTERB') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																                        			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
																                        	   END	
                                                                                                                   WHEN ConsCmbte."IdTipoComprobante" = 'FOE' AND :v_GlosaFacturaOtrosConceptosPeriodo = 'S' THEN IfNull(ConsCmbte."NombreConcepto" || ', ', '') || (SELECT "MES"
                                                                                                                                                                                                FROM (SELECT TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(2) "NUMERO",'Febrero' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(3) "NUMERO",'Marzo' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(4) "NUMERO",'Abril' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(5) "NUMERO",'Mayo' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(6) "NUMERO",'Junio' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(7) "NUMERO",'Julio' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(8) "NUMERO",'Agosto' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(9) "NUMERO",'Septiembre' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(10) "NUMERO",'Octubre' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(11) "NUMERO",'Noviembre' AS "MES" FROM DUMMY
                                                                                                                                                                                                      UNION ALL SELECT TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY
                                                                                                                                                                                                     )
                                                                                                                                                                                                WHERE "NUMERO" = TO_BIGINT(MONTH(ConsCmbte."FechaLiquidacion"))) || ' ' || Year(ConsCmbte."FechaLiquidacion") || '' || CASE 
                                                                                                                                                                                                                                                                                                                         WHEN LENGTH(ConsCmbteDet."Glosa") > 0 THEN (IfNull(', ' || ConsCmbteDet."Glosa", ''))
                                                                                                                                                                                                                                                                                                                       ELSE ''  
                                                                                                                                                                                                                                                                                                                       END 
                                                                                                                     WHEN ConsCmbte."IdTipoComprobante" = 'FOE' THEN IfNull(ConsCmbte."NombreConcepto", '') || CASE WHEN LENGTH(ConsCmbteDet."Glosa") > 0 THEN (IfNull(', ' || ConsCmbteDet."Glosa", '')) ELSE ''  END                                                                                                                                                                                                  
                                                                                                                     WHEN ConsCmbte."IdTipoInstrumento" = 'SIM' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteDet."IdTipoInstrumentoSubyacente" || ' ', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '')
                                                                                                                          || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ' ', '') || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Precio" ,'dx',2,'CL') || ' ', '')  || IfNull (To_varchar (ConsCmbte."FechaLiquidacion", 'DD-MM-YYYY')|| ' ' , ' ') 
                                                                                                                          || IfNull(ConsCmbte."IdBoveda" || ' ', '') || 'Nota: Operacion de Simultanea ' || IfNull( CondLiq."Codigo" ||' ','' ) || ', Tasa de Referencia ' || IFNULL("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Tasa", 'dx',2, 'CL')|| ' ', '')|| ', Nro. de dias '
                                                                                                                          || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero" (ConsCmbte."DiasPlazo", 'i',0,'CL') || ' ', '')|| ', Fecha Vencimiento ' || IfNull (To_varchar (ConsCmbte."FechaVencimiento", 'DD-MM-YYYY')|| ' ' , ' ')  
                                                                                                                          || CASE 
                                                                                                                               WHEN ConsCmbte."IdComprobanteReferencia" > 0 THEN ', Factura Asociada ' || ConsCmbte."IdComprobanteReferencia" 
                                                                                                                             ELSE '' 
                                                                                                                             END                                                           
                                                                                                                     WHEN ConsCmbte."IdTipoComprobante" = 'FAE' THEN 
                                                                                                                     	IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || 
                                                                                                                     	IfNull(ConsCmbteDet."FolioTransaccion" || ' ', '') || 
                                                                                                                     	IfNull(ConsCmbte."IdClasificacionTipoInstrumento" || ' ', '') || 
                                                                                                                     	CASE WHEN ConsCmbte."IdClasificacionTipoInstrumento" ='IF'  THEN ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(MovCom."ValorRescate",'dx',2, 'CL') || ' ', '') ELSE
                                                                                                                     	ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') END ||
                                                                                                                       	IfNull(InstEmisor."IdEmisor" || ' ', '') || 
                                                                                                                       	IfNull(ConsCmbte."Tasa" || '% ', '') || 
                                                                                                                       	CASE WHEN  ConsCmbte."IdClasificacionTipoInstrumento" ='IF' THEN IfNull(MovCom."IdEmisor" || ' ', '') ELSE
																												        IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ' ', '')
																														END ||
                                                                                                                       	IfNull(ConsCmbte."FechaVencimiento" || ' ', '') || 
                                                                                                                       	IfNull(ConsCmbte."IdBoveda" || ' ', '')
                                                                                                                ELSE ConsCmbte."NombreConcepto"
                                                                                                                END                                                                                                
                                                                                                                
                                                    WHEN ConsCmbte."IdTipoComprobante" IN ('NCR', 'NCE', 'NEE', 'NEO') THEN CASE
                                                                                                           WHEN ConsCmbte."IdConcepto" IN ('CMMNRV', 'ANVSPT','ANCSPT') AND LENGTH(ConsCmbte."Glosa") > 0 THEN 'ANULACION ' || ConsCmbte."Glosa"
                                                                                                           WHEN ConsCmbte."IdConcepto" = 'CMMNRV' THEN 'ANULACION COMISIÃ“N TRIMESTRAL NO APV - Acciones, CFI, ETFs - Trimestre'
                                                                                                           WHEN ConsCmbte."IdConcepto" = 'CMTAPV' AND LENGTH(ConsCmbte."Glosa") > 0 THEN 'ANULACION ' || ConsCmbte."Glosa"
                                                                                                           WHEN ConsCmbte."IdConcepto" = 'CMTAPV' THEN 'ANULACION COMISIÃ“N TRIMESTRAL APV - Acciones, CFI, ETFs - Trimestre'
                                                                                                           WHEN ConsCmbte."IdTipoInstrumento" = 'ACN' THEN 'ANULACION ' || IfNull(ConsCmbteDetNota."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por AcciÃ³n.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                                   WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                                   WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                                ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                                END
                                                                                                           WHEN ConsCmbte."IdTipoInstrumento" = 'CFI' THEN 'ANULACION ' || IfNull(ConsCmbteDetNota."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por CFI.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                             ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                             END
                                                                                                           WHEN ConsCmbte."IdTipoInstrumento" = 'ETN' THEN 'ANULACION ' || IfNull(ConsCmbteDetNota."AccionTipoOperacion" || ':', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || 'unidades de ' || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ', ', '') || 'a un valor de ' || CASE 
                                                                                                                                                                                                                                                                                                                                                                                                                          WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ' ', '')
                                                                                                                                                                                                                                                                                                                                                                                                                        ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ' ', '') 
                                                                                                                                                                                                                                                                                                                                                                                                                        END || 'por ETF.' || CASE
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(MarkMak."IdInstrumento") > 0 OR MarkMak."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                               WHEN length(Inst."IdInstrumento") > 0 OR Inst."IdInstrumento" > 0 THEN 'APB'
                                                                                                                                                                                                                                                                                                                                                                                                                                             ELSE ''
                                                                                                                                                                                                                                                                                                                                                                                                                                             END
                                                                                                           WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN 'ANULACION ' || CASE 
                                                                                                                                                                             WHEN ConsCmbteDetNota."AccionTipoOperacion" = 'COMPRA' THEN 'VENTA FX-COMPRA  '
                                                                                                                                                                           ELSE 'COMPRA FX-VENTA '
                                                                                                                                                                           END || CASE 
																		                        		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																		                        			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
																		                        		WHEN MovCom."NemotecnicoInstrumento" IN ('ARBDIS','ARBINT') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" <> OpSpt."IdComprobanteFacturacion" 
																		                        			THEN OpSpt."IdMonedaSecundaria" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaSecundaria" || '/' || OpSpt."IdMonedaPrincipal"
																		                        		WHEN MovCom."NemotecnicoInstrumento" IN ('DISTRI','INTERB') AND OpSpt."IdOperacionSpot" = MovCom."IdTransaccion" AND MovCom."IdMovimientoComprobante" = OpSpt."IdComprobanteFacturacion" 
																		                        			THEN OpSpt."IdMonedaPrincipal" || ' '  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || ' a un valor de (T/C)' || OpSpt."IdMonedaPrincipal" || '/' || OpSpt."IdMonedaSecundaria"
																		                        	   END
                                                                                                            WHEN ConsCmbte."IdTipoInstrumento" = 'SIM' THEN IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteDet."IdTipoInstrumentoSubyacente" || ' ', '') || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '')
                                                                                                                || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ' ', '') || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Precio" ,'dx',2,'CL') || ' ', '')  || IfNull (To_varchar (ConsCmbte."FechaLiquidacion", 'DD-MM-YYYY')|| ' ' , ' ') 
                                                                                                                || IfNull(ConsCmbte."IdBoveda" || ' ', '') || 'Nota: Operacion de Simultanea ' || IfNull( CondLiq."Codigo" ||' ','' ) || ', Tasa de Referencia ' || IFNULL("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Tasa", 'dx',2, 'CL')|| ' ', '')|| ', Nro. de dias '
                                                                                                                || IfNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero" (ConsCmbte."DiasPlazo", 'i',0,'CL') || ' ', '')|| ', Fecha Vencimiento ' || IfNull (To_varchar (ConsCmbte."FechaVencimiento", 'DD-MM-YYYY')|| ' ' , ' ')  
                                                                                                                || CASE 
                                                                                                                      WHEN ConsCmbte."IdComprobanteReferencia" > 0 THEN ', Factura Asociada ' || ConsCmbte."IdComprobanteReferencia" 
                                                                                                                    ELSE '' 
                                                                                                                    END	
                                                                                                         ELSE ConsCmbte."NombreConcepto"                                                                 
                                                                                                         END
                              ELSE                                
                                IfNull(ConsCmbteDet."AccionTipoOperacion" || ' ', '') || IfNull(ConsCmbteDet."IdTipoInstrumento" || ',', '')  || ifNull("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(ConsCmbteDet."Cantidad",'dx',2, 'CL') || ' ', '') || IfNull(ConsCmbteDet."NemotecnicoInstrumento" || ',', '') || CASE 
		                                                                                                                                                                                                                                                                                                                       WHEN ConsCmbteDet."IdMonedaLiquidacion" = 'USD' THEN ifNull(TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || MonLiq."Simbolo" || ',', '')
		                                                                                                                                                                                                                                                                                                                     ELSE ifNull(MonLiq."Simbolo" || TO_DECIMAL(ROUND(ConsCmbteDet."Precio", 2), 26, 2) || ',', '') 
		                                                                                                                                                                                                                                                                                                                     END || IfNull(ConsCmbteDet."FolioTransaccion" || ',', '') || ifnull(CASE 
		                                                                                                                                                                                                                                                                                                                     																	   WHEN TO_VARCHAR(ConsCmbteDet."FechaLiquidacion", 'DD-MM-YYYY') = '01-01-0001' THEN ''
		                                                                                                                                                                                                                                                                                                                     																	 ELSE TO_VARCHAR(ConsCmbteDet."FechaLiquidacion", 'DD-MM-YYYY')
		                                                                                                                                                                                                                                                                                                                     																	 END, '')
                              END As "IdComprobanteDetalleGlosa",
                              CASE
                                 WHEN ConsCmbteDet."AccionTipoOperacion" = 'RESCATE' THEN 'COMPRA'
                                 WHEN ConsCmbte."IdTipoComprobante" IN ('NCE', 'NEO', 'NEE') THEN ConsCmbteDetNota."AccionTipoOperacion"
                              ELSE 
                                 ConsCmbteDet."AccionTipoOperacion"
                              END As "IdComprobanteDetalleDescripcion",                                 
                              ConsCmbteDet."NroLinea" As "IdComprobanteDetalleNumero",                              
                              CASE 
                                WHEN ConsCmbte."IdConcepto" = 'COMMEN' THEN 1
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FOE', 'NEO') AND ConsCmbte."CantidadTotal" = 0 THEN 1
								WHEN ConsCmbteDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND ConsCmbte."IdTipoComprobante" = 'FAE' THEN MovCom."ValorRescate"
								WHEN ConsCmbteDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN ConsCmbteDet."ValorRescate"
                              ELSE ConsCmbteDet."Cantidad" 
                              END As "IdComprobanteDetalleCantidad",
                              CASE 
                                WHEN ConsCmbte."IdConcepto" = 'COMMEN' THEN ConsCmbteDet."MontoComprobanteDetalle"
                                WHEN ConsCmbte."IdTipoComprobante" IN ('FOE', 'NEO', 'FOX') AND ConsCmbte."Precio" = 0 THEN Coalesce(ConsCmbte."MontoLiquidacion",0)
                              ELSE ConsCmbteDet."Precio" 
                              END As "IdComprobanteDetallePrecio", 
                               0 As "IdComprobanteDetallePorcentaje",                               
                               0 As "IdComprobanteDetalleMontoDescuento",
                               --Case
                               --  When ConsCmbteDet."IdTipoComprobante" = 'NEO' Then 0
                               --Else ConsCmbteDet."MontoComprobanteDetalle" 
                               --End 
							   
							  CASE
									WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND UPPER(ConsCmbteDet."AccionTipoOperacion") = UPPER('VENTA') AND ConsCmbte."IdTipoComprobante" = 'FAE' THEN MovCom."MontoMovimientoComprobante"
									WHEN ConsCmbte."IdTipoComprobante" NOT IN ('NEO','NCE','NEE') AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' AND UPPER(ConsCmbteDet."AccionTipoOperacion") = UPPER('COMPRA') AND ConsCmbte."IdTipoComprobante" = 'FAE' THEN MovCom."MontoMovimientoComprobante"

									ELSE  ConsCmbteDet."MontoComprobanteDetalle"
								END AS  "IdComprobanteDetalleMonto",                     
                               Case
                                 When ConsCmbte."IdTipoComprobante" IN ('NEE', 'FOX') Then 1
                                 When ConsCmbte."IdTipoComprobante" IN ('FOE','NEO') AND Coalesce(CmbteMonADTE."MontoNoFacturable",0) = 0 Then 0  
                                 WHEN ConsCmbte."IdTipoComprobante" = 'FEE' And ConsCmbte."IdTipoInstrumento" = 'SPT' AND ConsCmbte."AccionTipoOperacion" = 'COMPRA' THEN 6
                                 WHEN ConsCmbte."IdTipoComprobante" = 'FEE' And ConsCmbte."IdTipoInstrumento" = 'SPT' AND ConsCmbte."AccionTipoOperacion" = 'VENTA' THEN 2                                                                     
                                 WHEN ConsCmbte."IdTipoComprobante" = 'FEE' And (ifnull(ConsCmbte."IdTipoInstrumento",'') != 'SPT'  ) THEN 1
                               Else 2                                   
                               End As "IdComprobanteDetalleEsExento",

                               CASE 
                                 WHEN ConsCmbteDet."IdTipoComprobante" = 'BOE' THEN Null
                               END AS "IdComprobanteDetalleBoletaEditable",
                               --Parametros Documentos                               
                               CASE 
                                  WHEN ConsCmbte."EsVentaCorta"= 'S' THEN ConsCmbte."NombreTipoProducto"  || ' VENTA CORTA'
                                  When ConsCmbteDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') Then '' 
                               ELSE ConsCmbte."NombreTipoProducto"
                               END AS "ParametroKeyProducto",
                               
                               -- Perï¿½
                               TipCmbte."TipoComprobanteDTEPE",
                               ConsCmbte."AccionTipoOperacion" AS "AccionTipoOperacionCmbtePE",
                               ConsCmbte."CodigoMoneda" AS "MonedaTransaccionPE",
                               ConsCmbte."CodigoMonedaLiquidacion" AS "MonedaLiquidacionPE",
                               ConsCmbte."FechaLiquidacion" AS "FechaLiquidacionPE",
                               ConsCmbte."NombreTipoProducto" AS "ProductoOperacionPE",
                               Pais."NombrePais" AS "NacionalidadReceptorPE",
                               ConsCmbte."CantidadTotal" AS "CantidadTotalPE",
                               CASE WHEN ConsCmbte."CantidadTotal" = 0 THEN 0 ELSE (ConsCmbte."MontoLiquidacion" / ConsCmbte."CantidadTotal")  END As "PrecioPromedioPE",
                               ConsCmbte."MontoLiquidacion" As "ImporteTotalPE",
                               ConsCmbte."MontoLiquidacion" AS "ImporteLiquidacionPE",
                               ConsCmbte."MontoFinal" As "TotalLiquidacionPE",
                               ConsCmbte."CodigoMonedaLiquidacion" As "MonedaImportePE",
                               CASE 
                                 WHEN ConsCmbte."MontoLiquidacionFinal" < 0 THEN ((ConsCmbte."MontoLiquidacionFinal") * -1)
                               ELSE ConsCmbte."MontoLiquidacionFinal"
                               END As "ImporteAPagarPE",
                                                                                            
                               ConsCmbte."NombreEmpleadoEjecutivoPortafolio" AS "CodigoVendedor",
                               
                               CASE 
			       WHEN ConsCmbte."ModPrefFact" IN ('MODPREFNORM','') AND ConsCmbteDet."IdTipoComprobante" NOT IN('FOE','NEO','FEE','NEE') THEN (ConsCmbte."TotalComisCmbte" + ConsCmbte."TotalDereCmbte" + ConsCmbte."TotalGastoCmbte" + ConsCmbte."TotalRetencionCmbte")
                                 WHEN ConsCmbteDet."IdTipoComprobante" IN ('FOE', 'NEO') AND (ConsCmbte."TotalComisCmbte" + ConsCmbte."TotalDereCmbte" + ConsCmbte."TotalGastoCmbte" + ConsCmbte."TotalRetencionCmbte" + ConsCmbte."TotalImptoCmbte") = 0 THEN Coalesce(CmbteMonADTE."MontoNoFacturable",0)
                                 WHEN ConsCmbteDet."IdTipoComprobante" IN ('FOE', 'NEO') AND ConsCmbte."TotalImptoCmbte" > 0 THEN ConsCmbte."MontoLiquidacion" 
                               ELSE 0
                               END AS "MontoNeto",                            
                               
                               ConsCmbte."TotalImptoCmbte" AS "MontoIva",   
                               
                               CASE
                                 WHEN ConsCmbteDet."IdTipoComprobante" = 'NEO' THEN ConsCmbteDet."MontoComprobanteDetalle"
                                 WHEN ConsCmbteDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'IF' THEN ConsCmbteDet."ValorRescate"
                                 WHEN ConsCmbteDet."Cantidad" !=0 AND ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' THEN ConsCmbteDet."Cantidad"
                               ELSE ConsCmbteDet."Cantidad"
                               END AS "PrecioOtraMoneda",    
                               
	                           Case
	                             When ConsCmbte."IdTipoInstrumento" in ('MIL', 'ACX', 'ETX') Then (1 / ConsCmbte."ValorTipoCambio")
	                             When ConsCmbte."IdTipoInstrumento" = 'SPT' Then 0
	                             When ConsCmbte."IdClasificacionTipoInstrumento" = 'RF' and ConsCmbte."IdMonedaLiquidacion" = 'USD' Then (1 / ConsCmbte."ValorTipoCambio")
	                             When ConsCmbte."ValorParidad" <= 1 Then 0
	                          Else ConsCmbte."ValorParidad"
	                          End As "FactorConversion",
	                           ConsCmbte."IdMonedaLiquidacion" AS "Moneda",
	                           CASE 
                                 When ConsCmbteDet."IdTipoOperacion" IN ('EMILIB', 'DIVACN') Then 0
                                 WHEN ConsCmbte."MontoLiquidacionFinal" < 0 THEN ((ConsCmbte."MontoLiquidacionFinal") * -1)
                               ELSE ConsCmbte."MontoLiquidacionFinal"
                               END AS "MontoLiquidacionFinal",
                               GrupDir."Direccion" AS "GrupoDireccionesCliente",
                               ConsCli."IdPersona" AS "IdPersona",
                               DirElec."IdDireccionElectronica" AS "IdDireccionElectronicaReceptor",
                               EmailRespaldo."Direccion" AS "EmailReceptorRespaldo",
                               CASE WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN MonSpt."CodigoBancoCentral" ELSE MonLiq."CodigoBancoCentral"  END  AS "CodigoMonedaBancoCentral",
                               CASE WHEN ConsCmbte."IdTipoInstrumento" = 'SPT' THEN MonSpt."Nombre" ELSE MonLiq."Nombre" END AS "NemoMoneda"
                      From "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" As ConsCmbteDet
                        Inner Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" As ConsCmbte
                           On ConsCmbte."IdComprobante" = ConsCmbteDet."IdComprobante"
                        Inner Join :v_ComprobanteAEnviar As CmbteAEnviar
                           On CmbteAEnviar."IdComprobante" = ConsCmbteDet."IdComprobante"
                        Inner Join :v_CmbteMontoADTE As CmbteMonADTE
                           On CmbteMonADTE."IdComprobante" = ConsCmbteDet."IdComprobante"
                          And CmbteMonADTE."IdTipoComprobante" = ConsCmbteDet."IdTipoComprobante"
                        Inner Join "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" As CondLiq
	                       On CondLiq."IdCondicionDeLiquidacion" = ConsCmbte."IdCondicionLiquidacion"  
                        Inner Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbte
                           On TipCmbte."IdTipoComprobante" = ConsCmbteDet."IdTipoComprobante"
                        LEFT JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OpSpot
                           On OpSpot."IdOperacionSpot" = ConsCmbteDet."IdTransaccion"  
                        Inner Join "BSCL.SocioNegocio.BD::CLI.Cliente" As Cliente
                           On Cliente."IdCliente" = ConsCmbte."IdCliente"
                         Inner Join  "BSCL.SocioNegocio.BD::PER.Persona" AS ConsCli
                           ON Cliente."IdPersona" = ConsCli."IdPersona"
                     INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENTCLI" 
                           ON ConsCli."IdDocumentoIdentidad" = "IDENTCLI"."IdDocumentoIdentidad"
                          Inner Join "BSCL.Portafolio.BD::POR.Portafolio" As Portafolio
                           On Portafolio."IdPortafolio" = ConsCmbte."IdPortafolio"                           
                        Inner Join "BSCL.Plataforma.BD::INS.TipoOperacion" As ConsTipOpe
	                       On ConsTipOpe."IdTipoOperacion" = ConsCmbteDet."IdTipoOperacion"	     
	                    Left Join "BSCL.Plataforma.BD::PLA.Moneda" As MonLiq
                           On MonLiq."IdMoneda" = ConsCmbteDet."IdMonedaLiquidacion"   
                      LEFT JOIN "BSCL"."BSCL.Plataforma.BD::PLA.Empresa" as EMP  
                            On EMP."IdEmpresa" = ConsCmbte."IdEmpresa"
                      LEFT JOIN BSCL."BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovCom
		            ON MovCom."IdComprobante" = ConsCmbte."IdComprobante"
			    AND (MovCom."IdTipoInstrumento" ='SPT' OR MovCom."IdClasificacionTipoInstrumento"= 'IF')
			  LEFT JOIN BSCL."BSCL.Mesa.BD::SPT.OperacionSpot" AS OpSpt 
		      	    ON MovCom."IdTransaccion" = OpSpt."IdOperacionSpot"	
        LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MonSpt
		      		  ON MonSpt."IdMoneda" = OpSpt."IdMonedaPrincipal"
			  LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC 
                           ON EMP."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
                        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as ConsEmp
                            ON ConsEmp."IdPersona"= SOC."IdPersona"
                     INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" 
                            ON ConsEmp."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
                        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS FIS
                            ON ConsEmp."IdDireccionFisicaPrincipal" = FIS."IdDireccionFisica" 
                        LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIU
                            ON FIS."IdCiudad" = CIU."IdCiudad"
                        LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS ELE
                            ON ConsEmp."IdDireccionElectronicaPrincipal" = ELE."IdDireccionElectronica"   
                        LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono"  AS TEL
                            ON ConsEmp."IdTelefonoPrincipal" = TEL."IdTelefono"    
                        LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais"  AS DIV
                            ON FIS."IdDivisionPais" = DIV."IdDivisionPais"
                        Left Join "BSCL.SocioNegocio.BD::PER.Persona" As ConsPer
                           On ConsPer."IdPersona" = ConsEmp."IdPersona"
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroEmisor
                           On GiroEmisor."IdGiro" = ConsPer."IdGiroPrincipal"   
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" As GiroReceptor
                           On GiroReceptor."IdGiro" = ConsCli."IdGiroPrincipal"
                        Left Join "BSCL.SocioNegocio.BD::PER.DireccionFisica" As DirFis
                           On DirFis."IdDireccionFisica" = ConsCli."IdDireccionFisicaPrincipal" 
                        Left Join "BSCL.Plataforma.BD::REG.Ciudad" As Ciudad
                           On Ciudad."IdCiudad" = DirFis."IdCiudad"
                        Left Join "BSCL.Plataforma.BD.Vistas::REG.ConsultarDivisionPais" As ConsDivPais
                           On ConsDivPais."IdDivisionPais" = DirFis."IdDivisionPais"
                          And ConsDivPais."IdCiudad" = Ciudad."IdCiudad"         
                        Left Join "BSCL.SocioNegocio.BD::PER.DireccionElectronica" As DirElec
                           On DirElec."IdDireccionElectronica" = ConsCli."IdDireccionElectronicaPrincipal"
                        LEFT JOIN 	(
	                        			SELECT "IdPersona", MAX("Direccion") AS "Direccion"
										FROM BSCL."BSCL.SocioNegocio.BD::PER.DireccionElectronica"
										WHERE "Borrado" = 'N' AND "Predeterminado" = 'S' AND "IdEstado" = 'ACT'
										GROUP BY "IdPersona"
									) AS EmailRespaldo
							ON EmailRespaldo."IdPersona" = ConsCli."IdPersona"
                        Left Join "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle" AS ConsCmbteDetNota
                           On ConsCmbteDetNota."IdComprobante" = ConsCmbte."IdComprobanteReferencia"
                          AND ConsCmbteDetNota."NroLinea" = ConsCmbteDet."NroLinea"  
                        Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" As TipCmbteNota
                           On TipCmbteNota."IdTipoComprobante" = ConsCmbteDetNota."IdTipoComprobante"
                        LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Inst
                           ON Inst."IdInstrumento" = ConsCmbteDet."IdInstrumento"
                          AND (Inst."PresenciaBursatilAjustada" >= v_IndicadorPresenciaBursatil OR ConsCmbte."IdClasificacionTipoInstrumento" = 'DV')
                        LEFT JOIN "BSCL.Plataforma.BD::INS.MarketMaker" AS MarkMak
                           On MarkMak."IdInstrumento" = ConsCmbteDet."IdInstrumento"
                          AND ((ConsCmbteDet."FechaComprobante" >= MarkMak."FechaInicio") AND (ConsCmbteDet."FechaComprobante" <= MarkMak."FechaFin"))
                          AND MarkMak."IdEstado" = 'ACT'
                        -- Perï¿½
                        LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" AS Pais
                           ON Pais."IdPais" = ConsCli."IdPaisNacionalidadPrincipal"       
                        LEFT JOIN "BSCL.Plataforma.BD::DER.Configuracion" AS ConfOpcion
                           ON ConfOpcion."NombreTipoParametro" = 'ModalidadOpcion' AND ConfOpcion."ValorParametro" = Inst."ModalidadLiquidacion"
                        LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS IndEnvMail
                           ON IndEnvMail."IdEntidad" = ConsCmbte."IdPortafolio"
                          AND IndEnvMail."IdTipoIndicador" = 24  
                        LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS InstEmisor
                           ON InstEmisor."IdInstrumento" = ConsCmbteDet."IdInstrumento"
                         LEFT JOIN :v_DirElectronica AS GrupDir
                          ON Cliente."IdCliente" = GrupDir."IdCliente"
                      Order by ConsCmbteDet."IdComprobante";
                      
        /*Select 'v_CmbteADTE', *
        from :v_CmbteADTE;*/
	  
                      
        Insert into "BSCL.Facturacion.BD::FAC.TareaDTE"
           (
             "IdTareaDTE",
             "IdTareaDTEGrupo",
             "TipoTareaDTE",
             "IdRespuestaDTE",
             "RespuestaDTEEnvioSII",             
             "RespuestaLocationDTE",
             "RespuestaPdfDTE",
             "RespuestaXmlDTE",
             "FolioComprobanteRespuestaDTE",             
             "IdComprobante",
             "IdEstadoTareaDTE",             
             "IdEstadoDTE",
             "IdEstadoSII",             
             "IdTipoComprobante",
             "TipoComprobanteDTE",
             "AccionTipoOperacion", 
             "PeriodoContable",
             "FechaComprobante",
             "FechaVencimiento",    
             "CodigoVendedor",
             "CodigoReceptor",
             "GlosaAjusteComision",
             "TotalComisionCmbte",
             "GlosaAjusteImpuesto",
             "TotalImpuestoCmbte",
             "GlosaAjusteDerecho",
             "TotalDerechoCmbte",
             "GlosaAjusteGasto",
             "TotalGastoCmbte",
             "GlosaAjusteRetencion",
             "TotalRetencionCmbte",
                   
             "IdComprobanteMontoNoFacturable",
             "MontoNeto",
             "IdComprobanteMontoExento",
             "IdComprobanteTasaIva",
             "MontoIva",
             "IdComprobanteMontoTotal",
             "IdComprobanteValorAPagar",
             
             "TotalIdTipoComprobanteMontoNeto",
             "TotalIdTipoComprobanteMontoExento",
             "TotalIdTipoComprobanteMontoIva",
             "TotalIdTipoComprobanteMontoTotal",
             "TotalIdTipoComprobanteCantidadDocumentos",
             "TotalIdTipoComprobanteCantidadAnulados",
             
             "DocumentoIdentidadEmisor",
             "RazonSocialEmisor",
             "GiroEmisor",
             "DireccionEmisor",
             "ComunaEmisor",
             "CiudadEmisor",
             "EmailEmisor",
             "CondicionPago",
             "TelefonoEmisor",
             "EmailReceptor",
             "DocumentoIdentidadReceptor",
             "CodigoSucursal",
             "RazonSocialReceptor",
             "GiroReceptor",
             "DireccionReceptor",
             "ComunaReceptor",
             "CiudadReceptor",
             "ContactoReceptor",
             
             --Referencias
             ----Referencia 1
             "TipoReferenciaIdComprobante",
             "FolioReferenciaIdComprobante",
             "FechaReferenciaIdComprobante",
             "RazonReferenciaIdComprobante",
             ----Referencia 2
             "TipoReferenciaFolioComprobante",
             "FolioReferenciaFolioComprobante",
             "FechaReferenciaFolioComprobante",
             "RazonReferenciaFolioComprobante",
             ----Referencia Notas
             "NumeroReferenciaNota",
             "TipoReferenciaNota",
             "FolioReferenciaNota",
             "FechaReferenciaNota",
             "RazonReferenciaNota",
             "CodigoReferenciaNota",
             --Detalle
             "IdComprobanteDetalleGlosa",
             "IdComprobanteDetalleDescripcion",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "PrecioOtraMoneda",
             "FactorConversion",
             "Moneda",
             "MontoLiquidacionFinal",
             "IdComprobanteDetalleEsExento",
             --Parametros
             "ParametroKeyProducto",
             
             --Peru
             "TipoComprobanteDTEPE",
             "AccionTipoOperacionCmbtePE",
             "MonedaTransaccionPE",
             "MonedaLiquidacionPE",
             "FechaLiquidacionPE",
             "ProductoOperacionPE",
             "NacionalidadReceptorPE",
             "CantidadTotalPE",
             "PrecioPromedioPE",
             "ImporteTotalPE",
             "ImporteLiquidacionPE",
             "TotalLiquidacionPE",
             "MonedaImportePE",
             "ImporteAPagarPE",
             "GrupoDireccionesCliente",
             "CodigoMonedaBancoCentral",
             "NemoMoneda"
           )
        Select
             "IdTareaDTE",
             "IdTareaDTEGrupo",
             "TipoTareaDTE",
             "IdRespuestaDTE",
             "RespuestaDTEEnvioSII",
             "RespuestaLocationDTE",
             "RespuestaPdfDTE",
             "RespuestaXmlDTE",
             "FolioComprobanteRespuestaDTE",
             "IdComprobante",
             "IdEstadoTareaDTE",
             "IdEstadoDTE",
             "IdEstadoSII",
             "IdTipoComprobante",
             "TipoComprobanteDTE",
             "AccionTipoOperacion",
             '',
             TO_DATE("FechaComprobante"),
             "FechaVencimiento", 
             "CodigoVendedor",
             "CodigoReceptor",
             "GlosaAjusteComision",
             "TotalComisionCmbte",
             "GlosaAjusteImpuesto",
             "TotalImpuestoCmbte",
             "GlosaAjusteDerecho",
             "TotalDerechoCmbte",
             "GlosaAjusteGasto",
             "TotalGastoCmbte",
             "GlosaAjusteRetencion",
             "TotalRetencionCmbte",
             
             "IdComprobanteMontoNoFacturable",
             "MontoNeto",
             "IdComprobanteMontoExento",
             "IdComprobanteTasaIva",
             "MontoIva",
             "IdComprobanteMontoTotal",
             "IdComprobanteValorAPagar",             
             0,
             0,
             0,
             0,
             0,
             0,
             "DocumentoIdentidadEmisor",
             "RazonSocialEmisor",
             "GiroEmisor",
             "DireccionEmisor",
             "ComunaEmisor",
             "CiudadEmisor",
             "EmailEmisor",
             "CondicionPago",
             "TelefonoEmisor",
             "EmailReceptor",
             "DocumentoIdentidadReceptor",
             "CodigoSucursal",
             "RazonSocialReceptor",
             "GiroReceptor",
             "DireccionReceptor",
             "ComunaReceptor",
             "CiudadReceptor",
             "ContactoReceptor",

             "TipoReferenciaIdComprobante",
             "FolioReferenciaIdComprobante",
             "FechaReferenciaIdComprobante",
             "RazonReferenciaIdComprobante",
             "TipoReferenciaFolioComprobante",
             "FolioReferenciaFolioComprobante",
             "FechaReferenciaFolioComprobante",
             "RazonReferenciaFolioComprobante",
             "NumeroReferenciaNota",
             "TipoReferenciaNota",
             "FolioReferenciaNota",
             "FechaReferenciaNota",
             "RazonReferenciaNota",
             "CodigoReferenciaNota",             
             "IdComprobanteDetalleGlosa",
             "IdComprobanteDetalleDescripcion",
             "IdComprobanteDetalleCantidad",
             "IdComprobanteDetallePrecio",
             "IdComprobanteDetallePorcentaje",
             "IdComprobanteDetalleMontoDescuento",
             "IdComprobanteDetalleMonto",
             "PrecioOtraMoneda",
             "FactorConversion",
             "Moneda",
             "MontoLiquidacionFinal",
             "IdComprobanteDetalleEsExento",
             "ParametroKeyProducto",
             
             "TipoComprobanteDTEPE",
             "AccionTipoOperacionCmbtePE",
             "MonedaTransaccionPE",
             "MonedaLiquidacionPE",
             "FechaLiquidacionPE",
             "ProductoOperacionPE",
             "NacionalidadReceptorPE",
             "CantidadTotalPE",
             "PrecioPromedioPE",
             "ImporteTotalPE",
             "ImporteLiquidacionPE",
             "TotalLiquidacionPE",
             "MonedaImportePE",
             "ImporteAPagarPE",
             "GrupoDireccionesCliente",
             "CodigoMonedaBancoCentral", 
             "NemoMoneda"
        From :v_CmbteADTE DTE;   
        
       
        
        Call "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma" 
         (			
			 /*p_IdEmpresa BIGINT*/ p_IdEmpresa => :p_IdEmpresa,
			 /*p_IdUsuario BIGINT*/ p_IdUsuario => :p_IdUsuario,
			 /*p_IdModulo NVARCHAR(3)*/ p_IdModulo => 'FAC',
		     /*p_IdAgenda BIGINT*/ p_IdAgenda => 0,
			 /*p_IdTipoTarea NVARCHAR(40)*/ p_IdTipoTarea => 'DTEENV',
			 /*p_Segundos BIGINT*/ p_Segundos => 0,
			 /*p_TablaParametros table*/ p_TablaParametros => :p_TablaParametros,
			 /*p_CreaParam NVARCHAR(1)*/ p_CreaParam => 'S',
			 /*p_out_IdPrograma table*/ p_out_IdPrograma => v_out_IdPrograma,
			 /*p_out_AdvertenciasContador int*/ p_out_AdvertenciasContador => v_out_AdvertenciasContador,
			 /*p_out_AdvertenciasDetalle table*/ p_out_AdvertenciasDetalle =>v_out_AdvertenciasDetalle,
			 /*p_out_ErroresContador int*/ p_out_ErroresContador => v_out_ErroresContador,
			 /*p_out_ErroresDetalle table*/ p_out_ErroresDetalle => v_out_ErroresDetalle
		 );	          
        /** Fin Preparacion Envio a DTE **/ 
        
        
        --*********************
		--* MENSAJE DE EXITO  *
		--*********************
        p_out_ResultadoProceso = Select "IdComprobante" As "Campo", "IdComprobante" As "Id",
				                        'BSCL.Facturacion.BD.Servicios::DteEnviarComprobante' As "Servicio",
				                        'ENV' As "Resultado"
		                         From :v_CmbteADTE Union All
		                         Select "IdComprobante" As "Campo", "IdComprobante" as "Id",
		                                'BSCL.Facturacion.BD.Servicios::DteEnviarComprobante' as "Servicio",
		                                'RECH' as "Resultado"
		                         From :v_RechazadosPorValidacion Union All
		                         Select "Campo", "Id", "Servicio", "Resultado" 
		                         From :p_out_ResultadoProceso; 
		                        
		
		
		
		Select Count("IdComprobante")
		Into Cant_ComprobanteEnviado
		from :v_CmbteADTE; 
		
		
		/*select Cant_ComprobanteGenerado As "Cant_ComprobanteGenerado"
		from dummy;*/	
		
		If Cant_ComprobanteEnviado > 0 Then 
		   v_TablaCodigosMensajes = Select Distinct "IdComprobante" As "IdInterno", 'UI_COMPROBANTE_ENVIADO' As "Codigo",
                                           'IdComprobante' As "CodigoDato1", Cast("IdComprobante" As NVARCHAR(520)) as "ValorDato1",
                                           '' As "CodigoDato2", '' As "ValorDato2" 
		                            From :v_CmbteADTE;
		  
  		   CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
		
		   p_out_InformacionDetalle = Select "Codigo", "Descripcion" 
		                              From :v_out_hash;
		
		   Select Count("Codigo") 
		   Into p_out_InformacionContador 
		   From :p_out_InformacionDetalle;
		End If;              

    END; --Logica
END;
