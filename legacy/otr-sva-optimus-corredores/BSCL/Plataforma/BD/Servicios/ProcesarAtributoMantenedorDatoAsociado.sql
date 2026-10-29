SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ProcesarAtributoMantenedorDatoAsociado";
GO
CREATE PROCEDURE
-- 
--  Proposito: Consultar los atributos de un mantenedor y sus datos asociados
--  Autor: Natalia Silva Muñoz
--  Fecha Creacion: 19/10/2015 
--
"BSCL.Plataforma.BD.Servicios::ProcesarAtributoMantenedorDatoAsociado"
  (
  	IN p_IdEmpresa	BIGINT,
    IN p_Mantenedor NVARCHAR(6),
    
    OUT v_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520))
  )
As

BEGIN
   Declare v_a             SMALLINT; 
   
   --MONEDA
   Declare Arr_CodigoDato, Arr_SimboloDato, Arr_CodigoPaisDato, Arr_NombreAtributo NVARCHAR(40) ARRAY;
   Declare Arr_NombreDato, Arr_NombrePaisDato, Arr_NombreEstadoDato, Arr_NombreTipoMoneda NVARCHAR(100) ARRAY;
   Declare Arr_IdPaisDato, Arr_MonedaSMV  BIGINT ARRAY;
   Declare Arr_IdTipoMoneda NVARCHAR(3) ARRAY;  
      
      
   Declare v_Arr_CodigoDato, v_Arr_SimboloDato, v_Arr_CodigoPaisDato, v_Arr_NombreAtributo NVARCHAR(40);
   Declare v_Arr_NombreDato, v_Arr_NombrePaisDato, v_Arr_NombreEstadoDato, v_Arr_NombreTipoMoneda NVARCHAR(100); 
   Declare v_Arr_IdPaisDato, v_Arr_MonedaSMV BIGINT;
   Declare v_Arr_IdTipoMoneda NVARCHAR(3);  
   
   --CONFIG
   Declare Arr_CodigoParametroDato NVARCHAR(40) ARRAY;
   Declare Arr_DescripcionDato, Arr_ValorDato NVARCHAR(520) ARRAY;
   Declare Arr_IdEmpresaDato BIGINT ARRAY;
   
   Declare v_Arr_CodigoParametroDato NVARCHAR(40);
   Declare v_Arr_DescripcionDato, v_Arr_ValorDato NVARCHAR(520); 
   Declare v_Arr_IdEmpresaDato BIGINT;   
   
   --ESTADO
   Declare Arr_IdMonedaDato, Arr_IdEstadoDato NVARCHAR(3) ARRAY;  
   
   Declare v_Arr_IdMonedaDato, v_Arr_IdEstadoDato NVARCHAR(3);    
   
   --MODULO 
   Declare Arr_IdModuloDato      NVARCHAR(3) ARRAY ;
   
   Declare v_Arr_IdModuloDato    NVARCHAR(3);
   
   --BOLSA
   Declare Arr_IdBolsaDato       NVARCHAR(3) ARRAY ;
   
   Declare v_Arr_IdBolsaDato     NVARCHAR(3);
   
   --MERCADO
   Declare Arr_IdMercadoDato     NVARCHAR(4) ARRAY; 
   
   Declare v_Arr_IdMercadoDato   NVARCHAR(4); 
   
   --TIPO INSTRUMENTO
   Declare Arr_IdTipoInstrumentoDato, Arr_IdClasificacionTipoInstrumentoDato NVARCHAR(3) ARRAY;
   Declare Arr_NombreClasificacionTipoInstrumentoDato  NVARCHAR(100) ARRAY;
   
   Declare v_Arr_IdTipoInstrumentoDato, v_Arr_IdClasificacionTipoInstrumentoDato NVARCHAR(3);
   Declare v_Arr_NombreClasificacionTipoInstrumentoDato  NVARCHAR(100);
   
   --PROPOSITO PORTAFOLIO
   Declare Arr_IdPropositoPortafolioDato NVARCHAR(40) ARRAY;
   Declare v_Arr_IdPropositoPortafolioDato NVARCHAR(40);
   
   --PAIS
   Declare Arr_EsOcde, Arr_EsUsPerson NVARCHAR(1) ARRAY;
   Declare v_Arr_EsOcde, v_Arr_EsUsPerson NVARCHAR(1);
    
   --REQUIERE AUTORIZACION
   DECLARE Arr_IdConcepto NVARCHAR(6) ARRAY;
   DECLARE v_Arr_IdConcepto NVARCHAR(6);
   DECLARE Arr_RequiereAutorizacion NVARCHAR(1) ARRAY;
   DECLARE v_Arr_RequiereAutorizacion NVARCHAR(1);
   DECLARE Arr_NombreModulo NVARCHAR(100) ARRAY;
   DECLARE v_Arr_NombreModulo NVARCHAR(100);
   DECLARE Arr_NombreConcepto NVARCHAR(100) ARRAY;
   DECLARE v_Arr_NombreConcepto NVARCHAR(100);
   
   --CUSTODIA
   DECLARE Arr_IdTipoPrestamoDato NVARCHAR(6) ARRAY;
   DECLARE v_Arr_IdTipoPrestamoDato NVARCHAR(6); 
   DECLARE Arr_CobraPrimaAlInicioDato NVARCHAR(1) ARRAY;
   DECLARE v_Arr_CobraPrimaAlInicioDato NVARCHAR(1);
   
   DECLARE Arr_MinimoDiasPrestamoDato BIGINT ARRAY;
   DECLARE v_Arr_MinimoDiasPrestamoDato BIGINT;
   DECLARE Arr_MaximoDiasPrestamoDato BIGINT ARRAY;
   DECLARE v_Arr_MaximoDiasPrestamoDato BIGINT;
   DECLARE Arr_SoloCuentaPropiaDato NVARCHAR(1) ARRAY;
   DECLARE v_Arr_SoloCuentaPropiaDato NVARCHAR(1);
   DECLARE Arr_PlazoSugeridoDato BIGINT ARRAY;
   DECLARE v_Arr_PlazoSugeridoDato BIGINT;
   DECLARE Arr_MinimoDiasAnticipoDato BIGINT ARRAY;
   DECLARE v_Arr_MinimoDiasAnticipoDato BIGINT;
   
   
   DECLARE Arr_IdTipoFirmanteDato NVARCHAR(40) ARRAY;
   DECLARE Arr_IdFirmanteDato 	NVARCHAR(40) ARRAY;
   DECLARE v_Arr_IdTipoFirmanteDato NVARCHAR(40);
   DECLARE v_Arr_IdFirmanteDato NVARCHAR(40);
   DECLARE Arr_CodigoParametro NVARCHAR(40) ARRAY;
   DECLARE Arr_Descripcion NVARCHAR(520) ARRAY;
   DECLARE Arr_Valor NVARCHAR(520) ARRAY;
   
   --- ESPECIES EN GARANTIA
   DECLARE Arr_IdEspecie BIGINT ARRAY;
   DECLARE v_Arr_IdEspecie BIGINT;
   DECLARE Arr_CodigoEspecie NVARCHAR(6) ARRAY;
   DECLARE v_Arr_CodigoEspecie NVARCHAR(6);
   DECLARE Arr_DescEspecie NVARCHAR(100) ARRAY;
   DECLARE v_Arr_DescEspecie NVARCHAR(100);
   DECLARE Arr_IdEstadoEspecie NVARCHAR(100) ARRAY;
   DECLARE v_Arr_IdEstadoEspecie NVARCHAR(100);
   

   DECLARE Arr_NombreFirmanteDato NVARCHAR(100) ARRAY;
   DECLARE Arr_NombreAgenteDato	  NVARCHAR(100) ARRAY;

   DECLARE v_Arr_NombreFirmanteDato NVARCHAR(100);
   DECLARE v_Arr_NombreAgenteDato NVARCHAR(100);

   DECLARE v_Arr_CodigoParametro NVARCHAR(40);
   DECLARE v_Arr_Descripcion NVARCHAR(520);
   DECLARE v_Arr_Valor NVARCHAR(520);

    -- MANTENEDOR DE BANCOS
	DECLARE Arr_IdBanco     				BIGINT        ARRAY;
	DECLARE Arr_NombreBanco 				NVARCHAR(100) ARRAY;
	DECLARE Arr_CodigoBanco 				NVARCHAR(40)  ARRAY;
	DECLARE Arr_IdPersona 					BIGINT  	  ARRAY;	
	DECLARE Arr_BancoReferenciaForward 		NVARCHAR(1) ARRAY;


	DECLARE Arr_IdContraparte				BIGINT  	  ARRAY;
	
	DECLARE Arr_NombrePersona 				NVARCHAR(150) ARRAY;
	DECLARE Arr_IdPaisOperacion 			BIGINT        ARRAY;
	DECLARE Arr_NombrePais					NVARCHAR(100) ARRAY;	
	DECLARE Arr_IdTipoBanco 				NVARCHAR(3)   ARRAY;
	DECLARE Arr_IdEstado 					NVARCHAR(3)   ARRAY;
	DECLARE Arr_IdEmpresa 					BIGINT        ARRAY;
	DECLARE Arr_IdSocioDeNegocio			BIGINT 		  ARRAY;
		
	DECLARE v_Arr_IdBanco 					BIGINT;
	DECLARE v_Arr_NombreBanco 				NVARCHAR(100);
	DECLARE v_Arr_BancoReferenciaForward 	NVARCHAR(1);

	DECLARE v_Arr_CodigoBanco				NVARCHAR(40);
	DECLARE v_Arr_IdPersona 				BIGINT;	

	DECLARE v_Arr_IdContraparte				BIGINT;
	
	DECLARE v_Arr_NombrePersona 			NVARCHAR(150);
	DECLARE v_Arr_IdPaisOperacion 			BIGINT;
	DECLARE v_Arr_NombrePais 				NVARCHAR(100);
	DECLARE v_Arr_IdTipoBanco 				NVARCHAR(3);
	DECLARE v_Arr_IdEstado 					NVARCHAR(3);
	DECLARE v_Arr_IdEmpresa 				BIGINT;
	DECLARE v_Arr_IdSocioDeNegocio			BIGINT;

    -- MANTENEDOR DE PLAZAS
	DECLARE Arr_IdPlaza						BIGINT	 		ARRAY;
	DECLARE Arr_Codigo						NVARCHAR(10) 	ARRAY;
	DECLARE Arr_NombrePlaza					NVARCHAR(100) 	ARRAY;
	DECLARE Arr_PlazoRetencionMismoBanco	BIGINT	 		ARRAY;
	DECLARE Arr_PlazoRetencionOtroBanco		BIGINT	 		ARRAY;
	DECLARE Arr_PlazoMaximoCobroMismoBanco	BIGINT	 		ARRAY;
	DECLARE Arr_PlazoMaximoCobroOtroBanco	BIGINT	 		ARRAY;
	DECLARE Arr_IdPais						BIGINT	 		ARRAY;
--	DECLARE Arr_NombrePais					NVARCHAR(100) 	ARRAY;
--	DECLARE Arr_IdEstado					NVARCHAR(3) 	ARRAY;
	DECLARE Arr_IdCiudad					BIGINT	 		ARRAY;
	DECLARE Arr_NombreCiudad				NVARCHAR(100) 	ARRAY;


	DECLARE v_Arr_IdPlaza						BIGINT;
	DECLARE v_Arr_Codigo						NVARCHAR(10);
	DECLARE v_Arr_NombrePlaza					NVARCHAR(100);
	DECLARE v_Arr_PlazoRetencionMismoBanco	BIGINT;
	DECLARE v_Arr_PlazoRetencionOtroBanco		BIGINT;
	DECLARE v_Arr_PlazoMaximoCobroMismoBanco	BIGINT;
	DECLARE v_Arr_PlazoMaximoCobroOtroBanco	BIGINT;
	DECLARE v_Arr_IdPais						BIGINT;
--	DECLARE v_Arr_NombrePais					NVARCHAR(100);
--	DECLARE v_Arr_IdEstado					NVARCHAR(3);
	DECLARE v_Arr_IdCiudad					BIGINT;
	DECLARE v_Arr_NombreCiudad				NVARCHAR(100);

	--Derivados
	DECLARE Arr_ModalidadEjercicioDato		NVARCHAR(100) 	ARRAY;    
	DECLARE Arr_ModalidadEjercicioDescDato	NVARCHAR(100) 	ARRAY;
    DECLARE Arr_TipoLiquidacionDato			NVARCHAR(100) 	ARRAY;
    DECLARE Arr_TipoLiquidacionDescDato		NVARCHAR(100) 	ARRAY;
	DECLARE Arr_ParametroEjercicioDato		NVARCHAR(100) 	ARRAY;
    DECLARE Arr_ParametroEjercicioDescDato	NVARCHAR(100) 	ARRAY;
	DECLARE Arr_ParametroEjercicioValor		NVARCHAR(100) 	ARRAY;	
	DECLARE v_Arr_ModalidadEjercicioDato		NVARCHAR(100);    
	DECLARE v_Arr_ModalidadEjercicioDescDato	NVARCHAR(100);
    DECLARE v_Arr_TipoLiquidacionDato			NVARCHAR(100);
    DECLARE v_Arr_TipoLiquidacionDescDato		NVARCHAR(100);
	DECLARE v_Arr_ParametroEjercicioDato		NVARCHAR(100);
    DECLARE v_Arr_ParametroEjercicioDescDato	NVARCHAR(100);
	DECLARE v_Arr_ParametroEjercicioValor		NVARCHAR(100);
	
	--Codigo Tipo Relacion
	DECLARE Arr_CodigoTipoRelacionDato			NVARCHAR(100) 	ARRAY;
	DECLARE Arr_DescripcionTipoRelacionDato		NVARCHAR(100) 	ARRAY;
	DECLARE v_Arr_CodigoTipoRelacionDato		NVARCHAR(100);
	DECLARE v_Arr_DescripcionTipoRelacionDato	NVARCHAR(100);


   v_Dato = select 0 As "NumFila", '' As "Campo", '' As "Valor" from dummy where 1=2;     
        

   -- MANTENEDOR DE MONEDAS
   IF UPPER(:p_Mantenedor) = 'MONEDA' THEN
   
	   	  v_DatoTablaMoneda = SELECT MON."IdMoneda", MON."Codigo", MON."Nombre", MON."Simbolo",
	                                 PAI."IdPais", PAI."CodigoPais", PAI."NombrePais",
	                                 EST."IdEstado", EST."Nombre" As "NombreEstado",
	                                 MON."IdTipoMoneda", TMO."Nombre" AS "NombreTipoMoneda",
	                                 MON."MonedaSMV"
	                          FROM "BSCL.Plataforma.BD::PLA.Moneda" AS MON
	                          LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoMoneda" AS TMO ON (TMO."IdTipoMoneda" = MON."IdTipoMoneda")	                          
	                          LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON (MON."IdPais" = PAI."IdPais")
	                          LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON (MON."IdEstado" = EST."IdEstado");              


	      Arr_IdMonedaDato		:= ARRAY_AGG(:v_DatoTablaMoneda."IdMoneda");          
	      Arr_CodigoDato 		:= ARRAY_AGG(:v_DatoTablaMoneda."Codigo");          
	      Arr_NombreDato 		:= ARRAY_AGG(:v_DatoTablaMoneda."Nombre");
	      Arr_SimboloDato 		:= ARRAY_AGG(:v_DatoTablaMoneda."Simbolo");
	      Arr_IdPaisDato 		:= ARRAY_AGG(:v_DatoTablaMoneda."IdPais");
	      Arr_CodigoPaisDato 	:= ARRAY_AGG(:v_DatoTablaMoneda."CodigoPais");
	      Arr_NombrePaisDato 	:= ARRAY_AGG(:v_DatoTablaMoneda."NombrePais");
	      Arr_IdEstadoDato 		:= ARRAY_AGG(:v_DatoTablaMoneda."IdEstado");
	      Arr_NombreEstadoDato 	:= ARRAY_AGG(:v_DatoTablaMoneda."NombreEstado");
	      Arr_IdTipoMoneda 		:= ARRAY_AGG(:v_DatoTablaMoneda."IdTipoMoneda");
	      Arr_NombreTipoMoneda	:= ARRAY_AGG(:v_DatoTablaMoneda."NombreTipoMoneda");
	      Arr_MonedaSMV			:= ARRAY_AGG(:v_DatoTablaMoneda."MonedaSMV");
	      
	      
	      FOR v_a IN 1 .. CARDINALITY(:Arr_IdMonedaDato) DO
	          v_Arr_IdMonedaDato 	 := :Arr_IdMonedaDato[:v_a];
	          v_Arr_CodigoDato 		 := :Arr_CodigoDato[:v_a];
	          v_Arr_NombreDato 		 := :Arr_NombreDato[:v_a];
	          v_Arr_SimboloDato 	 := :Arr_SimboloDato[:v_a];
	          v_Arr_IdPaisDato 		 := :Arr_IdPaisDato[:v_a];
	          v_Arr_CodigoPaisDato 	 := :Arr_CodigoPaisDato[:v_a];
	          v_Arr_NombrePaisDato 	 := :Arr_NombrePaisDato[:v_a];
	          v_Arr_IdEstadoDato 	 := :Arr_IdEstadoDato[:v_a];
	          v_Arr_NombreEstadoDato := :Arr_NombreEstadoDato[:v_a];
	          v_Arr_IdTipoMoneda 	 := :Arr_IdTipoMoneda[:v_a];
	          v_Arr_NombreTipoMoneda := :Arr_NombreTipoMoneda[:v_a];
	          v_Arr_MonedaSMV 		 := :Arr_MonedaSMV[:v_a];
	          

	        
	          v_Dato = Select v_a As "NumFila", 'IdMoneda' As "Campo", v_Arr_IdMonedaDato As "Valor"
	                   From dummy union all
	                   Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_CodigoDato As "Valor"
	                   From dummy union all
	                   Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
	                   From dummy union all 
	                   Select v_a As "NumFila", 'Simbolo' As "Campo", v_Arr_SimboloDato As "Valor"
	                   From dummy union all 
	                   Select v_a As "NumFila", 'IdPais' As "Campo", CAST(v_Arr_IdPaisDato As nvarchar(100)) As "Valor"
	                   From dummy union all 
	                   Select v_a As "NumFila", 'CodigoPais' As "Campo", v_Arr_CodigoPaisDato As "Valor"
	                   From dummy union all 
	                   Select v_a As "NumFila", 'NombrePais' As "Campo", v_Arr_NombrePaisDato As "Valor"
	                   From dummy union all
	                   Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
	                   From dummy union all
	                   Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor"     
	                   From dummy union all    
	                   Select v_a As "NumFila", 'IdTipoMoneda' As "Campo", v_Arr_IdTipoMoneda As "Valor"     
	                   From dummy union all    
	                   Select v_a As "NumFila", 'NombreTipoMoneda' As "Campo", v_Arr_NombreTipoMoneda As "Valor"     
	                   From dummy union all    
	                   Select v_a As "NumFila", 'MonedaSMV' As "Campo", CAST(v_Arr_MonedaSMV As nvarchar(100)) As "Valor"     
	                   From dummy union all
	                   Select "NumFila", "Campo", "Valor" From :v_Dato 	                   
	                   Order by "NumFila";     
	                                     
	      END FOR;
	END IF;   -- IF UPPER(:p_Mantenedor) = 'MONEDA'


	-- MANTENEDOR DE PARAMETROS DE CONFIGURACION
    IF UPPER(:p_Mantenedor) = 'CONFIG' THEN
        v_DatoTablaConfig = Select CONFIGURACION."IdEmpresa", CONFIGURACION."CodigoParametro", 
                                   CONFIGURACION."Descripcion", CONFIGURACION."Valor"
                            From "BSCL.Plataforma.BD::PLA.Configuracion" CONFIGURACION WHERE "IdEmpresa" = p_IdEmpresa; 
        
        Arr_IdEmpresaDato := ARRAY_AGG(:v_DatoTablaConfig."IdEmpresa");          
        Arr_CodigoParametroDato := ARRAY_AGG(:v_DatoTablaConfig."CodigoParametro");          
        Arr_DescripcionDato := ARRAY_AGG(:v_DatoTablaConfig."Descripcion");
        Arr_ValorDato := ARRAY_AGG(:v_DatoTablaConfig."Valor"); 
        FOR v_a IN 1 .. CARDINALITY(:Arr_CodigoParametroDato) DO
          v_Arr_IdEmpresaDato := :Arr_IdEmpresaDato[:v_a];
          v_Arr_CodigoParametroDato := :Arr_CodigoParametroDato[:v_a];
          v_Arr_DescripcionDato := :Arr_DescripcionDato[:v_a];
          v_Arr_ValorDato := :Arr_ValorDato[:v_a];              
          
          v_Dato = Select v_a As "NumFila", 'IdEmpresa' As "Campo",  CAST(v_Arr_IdEmpresaDato As NVARCHAR(100)) As "Valor"
                   From dummy union all
                   Select v_a As "NumFila", 'CodigoParametro' As "Campo", v_Arr_CodigoParametroDato As "Valor"
                   From dummy union all
                   Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_DescripcionDato As "Valor"
                   From dummy union all 
                   Select v_a As "NumFila", 'Valor' As "Campo", v_Arr_ValorDato As "Valor"
                   From dummy union all                       
                   Select "NumFila", "Campo", "Valor"
                   From :v_Dato
                   Order by "NumFila";                       
        END FOR;
	END IF;     -- IF UPPER(:p_Mantenedor) = 'CONFIG'


	-- MANTENEDOR DE ESTADOS
    IF UPPER(:p_Mantenedor) = 'ESTADO' THEN
          v_DatoTablaEstado = Select ESTADO."IdEstado", ESTADO."Nombre"
                              From "BSCL.Plataforma.BD::PLA.Estado" ESTADO ; 
        
          Arr_IdEstadoDato := ARRAY_AGG(:v_DatoTablaEstado."IdEstado");          
          Arr_NombreDato := ARRAY_AGG(:v_DatoTablaEstado."Nombre");
          FOR v_a IN 1 .. CARDINALITY(:Arr_IdEstadoDato) DO
             v_Arr_IdEstadoDato := :Arr_IdEstadoDato[:v_a];
             v_Arr_NombreDato := :Arr_NombreDato[:v_a];      
          
             v_Dato = Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
                      From dummy union all
                      Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
                      From dummy union all                       
                      Select "NumFila", "Campo", "Valor"
                      From :v_Dato
                      Order by "NumFila";                       
          END FOR;
	END IF;      -- IF UPPER(:p_Mantenedor) = 'ESTADO'


	-- MANTENEDOR DE MODULOS
    IF UPPER(:p_Mantenedor) = 'MODULO' THEN
            v_DatoTablaModulo = Select MODULO."IdModulo", MODULO."Codigo", MODULO."Nombre",
                                       MODULO."IdEstado", ESTADO."Nombre" As "NombreEstado"       
                                From "BSCL.Plataforma.BD::PLA.Modulo" MODULO
                                  Left Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO   
                                    On MODULO."IdEstado" = ESTADO."IdEstado";                                
        
            Arr_IdModuloDato := ARRAY_AGG(:v_DatoTablaModulo."IdModulo");
            Arr_CodigoDato := ARRAY_AGG(:v_DatoTablaModulo."Codigo");          
            Arr_NombreDato := ARRAY_AGG(:v_DatoTablaModulo."Nombre");
            Arr_IdEstadoDato := ARRAY_AGG(:v_DatoTablaModulo."IdEstado");
            Arr_NombreEstadoDato := ARRAY_AGG(:v_DatoTablaModulo."NombreEstado");
            FOR v_a IN 1 .. CARDINALITY(:Arr_IdModuloDato) DO
               v_Arr_IdModuloDato := :Arr_IdModuloDato[:v_a];
               v_Arr_CodigoDato := :Arr_CodigoDato[:v_a];
               v_Arr_NombreDato := :Arr_NombreDato[:v_a];      
               v_Arr_IdEstadoDato := :Arr_IdEstadoDato[:v_a];
               v_Arr_NombreEstadoDato := :Arr_NombreEstadoDato[:v_a];
          
               v_Dato = Select v_a As "NumFila", 'IdModulo' As "Campo", v_Arr_IdModuloDato As "Valor"
                        From dummy union all
                        Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_CodigoDato As "Valor"
                        From dummy union all
                        Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
                        From dummy union all     
                        Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
                        From dummy union all                       
                        Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor"
                        From dummy union all
                        Select "NumFila", "Campo", "Valor"
                        From :v_Dato
                        Order by "NumFila";                       
            END FOR;
	END IF;    -- IF UPPER(:p_Mantenedor) = 'MODULO'


	-- MANTENEDOR DE PAISES
    IF UPPER(:p_Mantenedor) = 'PAIS' THEN
          v_DatoTablaPais = Select PAIS."IdPais", 
          						   PAIS."NombrePais",
                                   PAIS."CodigoPais",
                                   PAIS."EsOcde",
                                   PAIS."EsPaisUs"
                            From "BSCL.Plataforma.BD::REG.Pais" PAIS;
                            
          Arr_IdPaisDato := ARRAY_AGG(:v_DatoTablaPais."IdPais");
          Arr_NombrePaisDato := ARRAY_AGG(:v_DatoTablaPais."NombrePais");                  
          Arr_CodigoPaisDato := ARRAY_AGG(:v_DatoTablaPais."CodigoPais");  
          Arr_EsOcde := ARRAY_AGG(:v_DatoTablaPais."EsOcde");
          Arr_EsUsPerson := ARRAY_AGG(:v_DatoTablaPais."EsPaisUs");
          
          FOR v_a IN 1 .. CARDINALITY(:Arr_IdPaisDato) DO
             v_Arr_IdPaisDato := :Arr_IdPaisDato[:v_a];
             v_Arr_NombrePaisDato := :Arr_NombrePaisDato[:v_a];
             v_Arr_CodigoPaisDato := :Arr_CodigoPaisDato[:v_a];      
             v_Arr_EsOcde := :Arr_EsOcde[:v_a];
         	 v_Arr_EsUsPerson := :Arr_EsUsPerson[:v_a];
          
             v_Dato = Select v_a As "NumFila", 'IdPais' As "Campo", CAST(v_Arr_IdPaisDato As NVARCHAR(100)) As "Valor"
                      From dummy union all
                      Select v_a As "NumFila", 'NombrePais' As "Campo", v_Arr_NombrePaisDato As "Valor"
                      From dummy union all
                      Select v_a As "NumFila", 'CodigoPais' As "Campo", v_Arr_CodigoPaisDato As "Valor"
                      From dummy union all
                      Select v_a As "NumFila", 'EsOcde' As "Campo", v_Arr_EsOcde As "Valor"
                      From dummy union all
                      Select v_a As "NumFila", 'EsPaisUs' As "Campo", v_Arr_EsUsPerson As "Valor"
                      From dummy union all
                      Select "NumFila", "Campo", "Valor"
                      From :v_Dato
                      Order by "NumFila";                       
          END FOR; 
	END IF;  -- IF UPPER(:p_Mantenedor) = 'PAIS'


	-- MANTENEDOR DE BOLSAS
    IF UPPER(:p_Mantenedor) = 'BOLSA' THEN
         v_DatoTablaBolsa = Select BOLSA."IdBolsa", BOLSA."Codigo", BOLSA."Nombre", 
                           PAIS."IdPais", PAIS."CodigoPais", PAIS."NombrePais",
                           ESTADO."IdEstado", ESTADO."Nombre" As "NombreEstado"
                  From "BSCL.Plataforma.BD::INS.Bolsa" BOLSA
                    Left Join "BSCL.Plataforma.BD::REG.Pais" PAIS 
                      On BOLSA."IdPais" = PAIS."IdPais"
                    Left Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO   
                      On BOLSA."IdEstado" = ESTADO."IdEstado";
                      
          Arr_IdBolsaDato := ARRAY_AGG(:v_DatoTablaBolsa."IdBolsa");          
          Arr_CodigoDato := ARRAY_AGG(:v_DatoTablaBolsa."Codigo");          
          Arr_NombreDato := ARRAY_AGG(:v_DatoTablaBolsa."Nombre");
          Arr_IdPaisDato := ARRAY_AGG(:v_DatoTablaBolsa."IdPais");
          Arr_CodigoPaisDato := ARRAY_AGG(:v_DatoTablaBolsa."CodigoPais");
          Arr_NombrePaisDato := ARRAY_AGG(:v_DatoTablaBolsa."NombrePais");
          Arr_IdEstadoDato := ARRAY_AGG(:v_DatoTablaBolsa."IdEstado");
          Arr_NombreEstadoDato := ARRAY_AGG(:v_DatoTablaBolsa."NombreEstado");
         
          FOR v_a IN 1 .. CARDINALITY(:Arr_IdBolsaDato) DO
              v_Arr_IdBolsaDato := :Arr_IdBolsaDato[:v_a];
              v_Arr_CodigoDato := :Arr_CodigoDato[:v_a];
              v_Arr_NombreDato := :Arr_NombreDato[:v_a];
              v_Arr_IdPaisDato := :Arr_IdPaisDato[:v_a];
              v_Arr_CodigoPaisDato := :Arr_CodigoPaisDato[:v_a];
              v_Arr_NombrePaisDato := :Arr_NombrePaisDato[:v_a];
              v_Arr_IdEstadoDato := :Arr_IdEstadoDato[:v_a];
              v_Arr_NombreEstadoDato := :Arr_NombreEstadoDato[:v_a];         

              v_Dato = Select v_a As "NumFila", 'IdBolsa' As "Campo", v_Arr_IdBolsaDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_CodigoDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'IdPais' As "Campo", CAST(v_Arr_IdPaisDato As nvarchar(100)) As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'CodigoPais' As "Campo", v_Arr_CodigoPaisDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'NombrePais' As "Campo", v_Arr_NombrePaisDato As "Valor"
                       From dummy union all                               
                       Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
                       From dummy union all                               
                       Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor"    
                       From dummy union all                               
                       Select "NumFila", "Campo", "Valor"
                       From :v_Dato
                       Order by "NumFila";
          END FOR;
	END IF;    
	
	
	-- MANTENEDOR DE MERCADOS
    IF UPPER(:p_Mantenedor) = 'MERCAD' THEN
            v_DatoTablaMercad = Select MERCAD."IdMercado", MERCAD."Codigo", MERCAD."Nombre", 
                                       MERCAD."Descripcion", 
                                       ESTADO."IdEstado", ESTADO."Nombre" As "NombreEstado"
                                From "BSCL.Plataforma.BD::INS.Mercado" MERCAD                            
                                  Left Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO   
                                    On MERCAD."IdEstado" = ESTADO."IdEstado";
             
            Arr_IdMercadoDato := ARRAY_AGG(:v_DatoTablaMercad."IdMercado");          
            Arr_CodigoDato := ARRAY_AGG(:v_DatoTablaMercad."Codigo");          
            Arr_NombreDato := ARRAY_AGG(:v_DatoTablaMercad."Nombre");
            Arr_DescripcionDato := ARRAY_AGG(:v_DatoTablaMercad."Descripcion");
            Arr_IdEstadoDato := ARRAY_AGG(:v_DatoTablaMercad."IdEstado");
            Arr_NombreEstadoDato := ARRAY_AGG(:v_DatoTablaMercad."NombreEstado");
            
            FOR v_a IN 1 .. CARDINALITY(:Arr_IdMercadoDato) DO
              v_Arr_IdMercadoDato := :Arr_IdMercadoDato[:v_a];
              v_Arr_CodigoDato := :Arr_CodigoDato[:v_a];
              v_Arr_NombreDato := :Arr_NombreDato[:v_a];
              v_Arr_DescripcionDato := :Arr_DescripcionDato[:v_a];
              v_Arr_IdEstadoDato := :Arr_IdEstadoDato[:v_a];
              v_Arr_NombreEstadoDato := :Arr_NombreEstadoDato[:v_a];         

              v_Dato = Select v_a As "NumFila", 'IdMercado' As "Campo", v_Arr_IdMercadoDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_CodigoDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_DescripcionDato As "Valor"
                       From dummy union all                                                              
                       Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
                       From dummy union all                               
                       Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor"    
                       From dummy union all                               
                       Select "NumFila", "Campo", "Valor"
                       From :v_Dato
                       Order by "NumFila";
            END FOR;
	END IF;  -- IF UPPER(:p_Mantenedor) = 'MERCAD'


	-- MANTENEDOR DE TIPO INSTRUMENTO
	IF UPPER(:p_Mantenedor) = 'TIPINS' THEN
           v_DatoTablaTipIns = Select TIPINS."IdTipoInstrumento", TIPINS."Codigo", TIPINS."Nombre", 
                                   TIPINS."Descripcion", 
                                   ESTADO."IdEstado", ESTADO."Nombre" As "NombreEstado",
                                   CLASTIPINS."IdClasificacionTipoInstrumento",
                                   CLASTIPINS."Nombre" As "NombreClasificacionTipoInstrumento"                          
                            From "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS
                              Left Join "BSCL.Plataforma.BD::PLA.Estado" ESTADO   
                                On TIPINS."IdEstado" = ESTADO."IdEstado"
                              Left Join "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLASTIPINS
                                On TIPINS."IdClasificacionTipoInstrumento" = CLASTIPINS."IdClasificacionTipoInstrumento";
           
           Arr_IdTipoInstrumentoDato := ARRAY_AGG(:v_DatoTablaTipIns."IdTipoInstrumento");          
           Arr_CodigoDato := ARRAY_AGG(:v_DatoTablaTipIns."Codigo");          
           Arr_NombreDato := ARRAY_AGG(:v_DatoTablaTipIns."Nombre");
           Arr_DescripcionDato := ARRAY_AGG(:v_DatoTablaTipIns."Descripcion");
           Arr_IdEstadoDato := ARRAY_AGG(:v_DatoTablaTipIns."IdEstado");
           Arr_NombreEstadoDato := ARRAY_AGG(:v_DatoTablaTipIns."NombreEstado");
           Arr_IdClasificacionTipoInstrumentoDato := ARRAY_AGG(:v_DatoTablaTipIns."IdClasificacionTipoInstrumento");
           Arr_NombreClasificacionTipoInstrumentoDato := ARRAY_AGG(:v_DatoTablaTipIns."NombreClasificacionTipoInstrumento");
        
           FOR v_a IN 1 .. CARDINALITY(:Arr_IdTipoInstrumentoDato) DO
               v_Arr_IdTipoInstrumentoDato := :Arr_IdTipoInstrumentoDato[:v_a];
               v_Arr_CodigoDato := :Arr_CodigoDato[:v_a];
               v_Arr_NombreDato := :Arr_NombreDato[:v_a];
               v_Arr_DescripcionDato := :Arr_DescripcionDato[:v_a];
               v_Arr_IdEstadoDato := :Arr_IdEstadoDato[:v_a];
               v_Arr_NombreEstadoDato := :Arr_NombreEstadoDato[:v_a];         
               v_Arr_IdClasificacionTipoInstrumentoDato := :Arr_IdClasificacionTipoInstrumentoDato[:v_a];
               v_Arr_NombreClasificacionTipoInstrumentoDato := :Arr_NombreClasificacionTipoInstrumentoDato[:v_a];

               v_Dato = Select v_a As "NumFila", 'IdTipoInstrumento' As "Campo", v_Arr_IdTipoInstrumentoDato As "Valor"
                        From dummy union all
                        Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_CodigoDato As "Valor"
                        From dummy union all
                        Select v_a As "NumFila", 'Nombre' As "Campo", v_Arr_NombreDato As "Valor"
                        From dummy union all 
                        Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_DescripcionDato As "Valor"
                        From dummy union all                                                              
                        Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor"
                        From dummy union all                               
                        Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor"
                        From dummy union all
                        Select v_a As "NumFila", 'IdClasificacionTipoInstrumento' As "Campo", v_Arr_IdClasificacionTipoInstrumentoDato As "Valor"    
                        From dummy union all  
                        Select v_a As "NumFila", 'NombreClasificacionTipoInstrumento' As "Campo", v_Arr_NombreClasificacionTipoInstrumentoDato As "Valor"    
                        From dummy union all                                                                   
                        Select "NumFila", "Campo", "Valor"
                        From :v_Dato
                        Order by "NumFila";
           END FOR;
	END IF;    --	IF UPPER(:p_Mantenedor) = 'TIPINS'


	-- MANTENEDOR DE PROPOSITOS PORTAFOLIO
    IF UPPER(:p_Mantenedor) = 'PROPOS' THEN
        v_DatoTablaPropos = Select PROPOS."IdPropositoPortafolio", PROPOS."Descripcion", PROPOS."IdEstado",
                                   ESTADO."Nombre" As "NombreEstado"       
                              From "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" As PROPOS
                              Left Join "BSCL.Plataforma.BD::PLA.Estado" As ESTADO On PROPOS."IdEstado" = ESTADO."IdEstado";                                
    
        Arr_IdPropositoPortafolioDato	:= ARRAY_AGG(:v_DatoTablaPropos."IdPropositoPortafolio");
        Arr_DescripcionDato				:= ARRAY_AGG(:v_DatoTablaPropos."Descripcion");   
        Arr_IdEstadoDato				:= ARRAY_AGG(:v_DatoTablaPropos."IdEstado");
        Arr_NombreEstadoDato			:= ARRAY_AGG(:v_DatoTablaPropos."NombreEstado");
        FOR v_a IN 1 .. CARDINALITY(:Arr_IdPropositoPortafolioDato) DO
        	v_Arr_IdPropositoPortafolioDato	:= :Arr_IdPropositoPortafolioDato[:v_a];
        	v_Arr_DescripcionDato			:= :Arr_DescripcionDato[:v_a];
        	v_Arr_IdEstadoDato				:= :Arr_IdEstadoDato[:v_a];
        	v_Arr_NombreEstadoDato			:= :Arr_NombreEstadoDato[:v_a];
        	
        	v_Dato = Select v_a As "NumFila", 'IdPropositoPortafolio' As "Campo", v_Arr_IdPropositoPortafolioDato As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_DescripcionDato As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstadoDato As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'NombreEstado' As "Campo", v_Arr_NombreEstadoDato As "Valor" From dummy union all
        			 Select "NumFila", "Campo", "Valor" From :v_Dato
                     Order by "NumFila";
        END FOR;
	END IF;   -- IF UPPER(:p_Mantenedor) = 'PROPOS'


	-- MANTENEDOR DE AUTORIZACION
    IF UPPER(:p_Mantenedor) = 'PORTAF' THEN
        v_DatoTablaPortaf = SELECT R."IdConcepto", "RequiereAutorizacion",
        					 --M."Nombre" as "NombreModulo", 
        					 Cast('' As NVARCHAR(100)) As "NombreModulo", 
        					 C."Nombre" as "NombreConcepto"
							FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" R
							left JOIN "BSCL.Plataforma.BD::PLA.Concepto" C ON R."IdConcepto" = C."IdConcepto"
							where R."IdEmpresa" = :p_IdEmpresa
							--LEFT JOIN "BSCL.Plataforma.BD::PLA.Modulo" M ON C."IdModulo" = M."IdModulo" 
							--ORDER BY M."Nombre", R."IdConcepto";
                            ORDER BY R."IdConcepto";
        				            
    
        Arr_IdConcepto					:= ARRAY_AGG(:v_DatoTablaPortaf."IdConcepto");
        Arr_RequiereAutorizacion		:= ARRAY_AGG(:v_DatoTablaPortaf."RequiereAutorizacion");   
        Arr_NombreModulo		:= ARRAY_AGG(:v_DatoTablaPortaf."NombreModulo");  
        Arr_NombreConcepto		:= ARRAY_AGG(:v_DatoTablaPortaf."NombreConcepto");  
        
        FOR v_a IN 1 .. CARDINALITY(:Arr_IdConcepto) DO
        	v_Arr_IdConcepto				:= :Arr_IdConcepto[:v_a];
        	v_Arr_RequiereAutorizacion		:= :Arr_RequiereAutorizacion[:v_a];
        	v_Arr_NombreModulo				:= :Arr_NombreModulo[:v_a];
        	v_Arr_NombreConcepto			:= :Arr_NombreConcepto[:v_a];
        					            	
        	v_Dato = Select v_a As "NumFila", 'IdConcepto' As "Campo", v_Arr_IdConcepto As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'RequiereAutorizacion' As "Campo", v_Arr_RequiereAutorizacion As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'NombreModulo' As "Campo", v_Arr_NombreModulo As "Valor" From dummy union all
        			 Select v_a As "NumFila", 'NombreConcepto' As "Campo", v_Arr_NombreConcepto As "Valor" From dummy union all
        			 Select "NumFila", "Campo", "Valor" From :v_Dato
                     Order by "NumFila";
        END FOR;
	END IF;   -- IF UPPER(:p_Mantenedor) = 'PORTAF'


	-- MANTENEDOR TIPO PRESTAMO
    IF UPPER(:p_Mantenedor) = 'PRESTA' THEN
              v_DatoTablaCusPres = Select TipPres."IdTipoPrestamo", TipPres."CobraPrimaAlInicio", TipPres."MinimoDiasPrestamo", 
              							TipPres."MaximoDiasPrestamo", TipPres."SoloCuentaPropia", TipPres."PlazoSugerido", 
              							TipPres."MinimoDiasAnticipo" 
              						From "BSCL.Custodia.BD::PRT.TipoPrestamo" TipPres;
   
   
              Arr_IdTipoPrestamoDato := ARRAY_AGG(:v_DatoTablaCusPres."IdTipoPrestamo");
              Arr_CobraPrimaAlInicioDato := ARRAY_AGG(:v_DatoTablaCusPres."CobraPrimaAlInicio");
              Arr_MinimoDiasPrestamoDato := ARRAY_AGG(:v_DatoTablaCusPres."MinimoDiasPrestamo");
              Arr_MaximoDiasPrestamoDato := ARRAY_AGG(:v_DatoTablaCusPres."MaximoDiasPrestamo");
              Arr_SoloCuentaPropiaDato := ARRAY_AGG(:v_DatoTablaCusPres."SoloCuentaPropia");
              Arr_PlazoSugeridoDato := ARRAY_AGG(:v_DatoTablaCusPres."PlazoSugerido");
              Arr_MinimoDiasAnticipoDato := ARRAY_AGG(:v_DatoTablaCusPres."MinimoDiasAnticipo");
              
              FOR v_a IN 1 .. CARDINALITY(:Arr_CobraPrimaAlInicioDato) DO
                  v_Arr_IdTipoPrestamoDato := :Arr_IdTipoPrestamoDato[:v_a]; 
                  v_Arr_CobraPrimaAlInicioDato := :Arr_CobraPrimaAlInicioDato[:v_a];
                  v_Arr_MinimoDiasPrestamoDato := :Arr_MinimoDiasPrestamoDato[:v_a];
                  v_Arr_MaximoDiasPrestamoDato := :Arr_MaximoDiasPrestamoDato[:v_a];
                  v_Arr_SoloCuentaPropiaDato := :Arr_SoloCuentaPropiaDato[:v_a];
                  v_Arr_PlazoSugeridoDato := :Arr_PlazoSugeridoDato[:v_a];
                  v_Arr_MinimoDiasAnticipoDato := :Arr_MinimoDiasAnticipoDato[:v_a];
                  
                  v_Dato = Select v_a As "NumFila", 'IdTipoPrestamo' As "Campo", v_Arr_IdTipoPrestamoDato As "Valor"
                           From dummy union all
                           Select v_a As "NumFila", 'CobraPrimaAlInicio' As "Campo", v_Arr_CobraPrimaAlInicioDato As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'MinimoDiasPrestamo' As "Campo", CAST(v_Arr_MinimoDiasPrestamoDato as NVARCHAR(100)) As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'MaximoDiasPrestamo' As "Campo", CAST(v_Arr_MaximoDiasPrestamoDato as NVARCHAR(100)) As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'SoloCuentaPropia' As "Campo", v_Arr_SoloCuentaPropiaDato As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'PlazoSugerido' As "Campo", CAST(v_Arr_PlazoSugeridoDato as NVARCHAR(100)) As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'MinimoDiasAnticipo' As "Campo", CAST(v_Arr_MinimoDiasAnticipoDato as NVARCHAR(100)) As "Valor"
                           From dummy union all                                                
                           
                           Select "NumFila", "Campo", "Valor"
                           From :v_Dato
                           Order by "NumFila";
              END FOR;	
	END IF;    -- IF UPPER(:p_Mantenedor) = 'PRESTA'


	-- MANTENEDOR FIRMANTES TRASPASOS
    IF UPPER(:p_Mantenedor) = 'FIRMAN' THEN
                v_DatoTablaCusFir = Select "IdTipoFirmante","IdFirmante", "NombreFirmante", "NombreAgente"
                                    From "BSCL.Custodia.BD.Vistas::CUS.ConsultarFirmantesTraspaso" TipFir;
   
                Arr_IdTipoFirmanteDato 	:= ARRAY_AGG(:v_DatoTablaCusFir."IdTipoFirmante");
                Arr_IdFirmanteDato 		:= ARRAY_AGG(:v_DatoTablaCusFir."IdFirmante");
                Arr_NombreFirmanteDato  := ARRAY_AGG(:v_DatoTablaCusFir."NombreFirmante");
                Arr_NombreAgenteDato 	:= ARRAY_AGG(:v_DatoTablaCusFir."NombreAgente");
              
                FOR v_a IN 1 .. CARDINALITY(:Arr_IdTipoFirmanteDato) DO
                    v_Arr_IdTipoFirmanteDato 	:= :Arr_IdTipoFirmanteDato[:v_a]; 
                    v_Arr_IdFirmanteDato 		:= :Arr_IdFirmanteDato[:v_a];
                    v_Arr_NombreFirmanteDato 	:= :Arr_NombreFirmanteDato[:v_a];
                    v_Arr_NombreAgenteDato 		:= :Arr_NombreAgenteDato[:v_a];
                  
                    v_Dato = Select v_a As "NumFila", 'IdTipoFirmante' As "Campo", 	v_Arr_IdTipoFirmanteDato As "Valor"
                            From dummy union all
                            
		                    Select v_a As "NumFila", 'IdFirmante' As "Campo", 		v_Arr_IdFirmanteDato As "Valor"
						    From dummy union all 

		                    Select v_a As "NumFila", 'NombreFirmante' As "Campo",	 v_Arr_NombreFirmanteDato As "Valor"
						    From dummy union all 


		                    Select v_a As "NumFila", 'NombreAgente' As "Campo", 	v_Arr_NombreAgenteDato As "Valor"
						    From dummy union all 
						    
                            Select "NumFila", "Campo", "Valor"
                            From :v_Dato
                            Order by "NumFila";
                END FOR;
	END IF;  -- IF UPPER(:p_Mantenedor) = 'FIRMAN'


	-- MANTENEDOR CONFIGURACION CUSTODIA(PARAMETROS)
 	IF UPPER(:p_Mantenedor) = 'CTADCV' THEN
        v_DatoTablaCuentaDCV =	
        		Select
        			CAST(CuentaDCV."CodigoParametro" AS NVARCHAR(40)) AS "CodigoParametro" ,
        			CAST(CuentaDCV."Descripcion" AS NVARCHAR(520)) AS "Descripcion",
        			CAST(CuentaDCV."Valor" AS NVARCHAR(520)) AS "Valor"
				From "BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia" CuentaDCV WHERE "IdEmpresa" = p_IdEmpresa;

		Arr_CodigoParametro 	:= ARRAY_AGG(:v_DatoTablaCuentaDCV."CodigoParametro");
		Arr_Descripcion 		:= ARRAY_AGG(:v_DatoTablaCuentaDCV."Descripcion");
		Arr_Valor			 	:= ARRAY_AGG(:v_DatoTablaCuentaDCV."Valor");

		FOR v_a IN 1 .. CARDINALITY(:Arr_CodigoParametro) DO
			v_Arr_CodigoParametro 	:= :Arr_CodigoParametro[:v_a];
			v_Arr_Descripcion 		:= :Arr_Descripcion[:v_a];
			v_Arr_Valor 			:= :Arr_Valor[:v_a];
			
			v_Dato = 	Select v_a As "NumFila", 'CodigoParametro' As "Campo", v_Arr_CodigoParametro As "Valor"
                		From dummy union all
                
	                    Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_Descripcion As "Valor"
					    From dummy union all 

	                    Select v_a As "NumFila", 'Valor' As "Campo", v_Arr_Valor As "Valor"
					    From dummy union all 
			    
                Select "NumFila", "Campo", "Valor"
                From :v_Dato
                Order by "NumFila";
              
		End For;	
	END IF;  -- IF UPPER(:p_Mantenedor) = 'CTADCV'
					
					
 	-- MANTENEDOR DE BANCOS
 	IF UPPER(:p_Mantenedor) = 'BANCO' THEN
        v_DatoTablaBanco =	
        		Select CB."IdBanco", CB."NombreBanco", CB."CodigoBanco",
					CB."IdPersona",CB."IdSocioDeNegocio",CB."NombrePersona", CB."IdContraparte",
					    CB."IdPaisOperacion", CB."NombrePais", CB."IdEstado", CB."IdEmpresa", CB."IdTipoBanco", CB."BancoReferenciaForward"
				FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" CB
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" CP ON CP."IdPersona" = CB."IdPersona"
				WHERE "IdEmpresa" = p_IdEmpresa
				Order by "IdBanco";
					
		
		Arr_IdBanco 			:= ARRAY_AGG(:v_DatoTablaBanco."IdBanco");
		Arr_NombreBanco 		:= ARRAY_AGG(:v_DatoTablaBanco."NombreBanco");
		Arr_BancoReferenciaForward 		:= ARRAY_AGG(:v_DatoTablaBanco."BancoReferenciaForward");
		Arr_CodigoBanco			:= ARRAY_AGG(:v_DatoTablaBanco."CodigoBanco");
		Arr_IdPersona 			:= ARRAY_AGG(:v_DatoTablaBanco."IdPersona");
		Arr_IdContraparte		:= ARRAY_AGG(:v_DatoTablaBanco."IdContraparte");
		Arr_NombrePersona 		:= ARRAY_AGG(:v_DatoTablaBanco."NombrePersona");
		Arr_IdPaisOperacion 	:= ARRAY_AGG(:v_DatoTablaBanco."IdPaisOperacion");
		Arr_NombrePais	        := ARRAY_AGG(:v_DatoTablaBanco."NombrePais");
		Arr_IdTipoBanco 		:= ARRAY_AGG(:v_DatoTablaBanco."IdTipoBanco");
		Arr_IdEstado 			:= ARRAY_AGG(:v_DatoTablaBanco."IdEstado");
		Arr_IdEmpresa 			:= ARRAY_AGG(:v_DatoTablaBanco."IdEmpresa");
		Arr_IdSocioDeNegocio    := ARRAY_AGG(:v_DatoTablaBanco."IdSocioDeNegocio");

		FOR v_a IN 1 .. CARDINALITY(:Arr_IdBanco) DO
			v_Arr_IdBanco 			:= :Arr_IdBanco[:v_a];
			v_Arr_NombreBanco 		:= :Arr_NombreBanco[:v_a];
			v_Arr_BancoReferenciaForward 		:= :Arr_BancoReferenciaForward[:v_a];
			v_Arr_CodigoBanco		:= :Arr_CodigoBanco[:v_a];
			v_Arr_IdPersona 		:= :Arr_IdPersona[:v_a];
			v_Arr_IdContraparte 	:= :Arr_IdContraparte[:v_a];
			v_Arr_NombrePersona 	:= :Arr_NombrePersona[:v_a];
			v_Arr_IdPaisOperacion	:= :Arr_IdPaisOperacion[:v_a];
			v_Arr_NombrePais 		:= :Arr_NombrePais[:v_a];											
			v_Arr_IdTipoBanco 		:= :Arr_IdTipoBanco[:v_a];
			v_Arr_IdEstado 			:= :Arr_IdEstado[:v_a];
			v_Arr_IdEmpresa 		:= :Arr_IdEmpresa[:v_a];
			v_Arr_IdSocioDeNegocio  := :Arr_IdSocioDeNegocio[:v_a];
			
			v_Dato = 	Select v_a As "NumFila", 'IdBanco' As "Campo", CAST(v_Arr_IdBanco As nvarchar(40)) As "Valor"
                		From dummy union all                                                		
	                    Select v_a As "NumFila", 'NombreBanco' As "Campo", v_Arr_NombreBanco As "Valor"
					    From dummy union all
					    Select v_a As "NumFila", 'BancoReferenciaForward' As "Campo", v_Arr_BancoReferenciaForward As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'CodigoBanco' As "Campo", v_Arr_CodigoBanco As "Valor"
					    From dummy union all		    
	                    Select v_a As "NumFila", 'IdPersona' As "Campo", CAST(v_Arr_IdPersona As nvarchar(40)) As "Valor"
	                    From dummy union ALL
	                    Select v_a As "NumFila", 'IdContraparte' As "Campo", CAST(v_Arr_IdContraparte As nvarchar(40)) As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'NombrePersona' As "Campo", v_Arr_NombrePersona As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'IdPaisOperacion' As "Campo", CAST(v_Arr_IdPaisOperacion As nvarchar(520)) As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'NombrePais' As "Campo", v_Arr_NombrePais As "Valor"
					    From dummy union all													    
	                    Select v_a As "NumFila", 'IdTipoBanco' As "Campo", v_Arr_IdTipoBanco As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstado As "Valor"
					    From dummy union all
	                    Select v_a As "NumFila", 'IdEmpresa' As "Campo", CAST(v_Arr_IdEmpresa As nvarchar(40)) As "Valor"
					    From dummy union all
			    		Select v_a As "NumFila", 'IdSocioDeNegocio' As "Campo", CAST(v_Arr_IdSocioDeNegocio As nvarchar(40)) As "Valor"
	                    From dummy union ALL
					    
                Select "NumFila", "Campo", "Valor"
                From :v_Dato
                Order by "NumFila";
              
		END FOR;										
 	END IF; -- IF UPPER(:p_Mantenedor) = 'BANCO'


 	-- MANTENEDOR DE PLAZAS
 	IF UPPER(:p_Mantenedor) = 'PLAZA' THEN
        v_DatoTablaPlaza =	
        		Select "IdPlaza", "Codigo", "NombrePlaza", "PlazoRetencionMismoBanco",
						"PlazoRetencionOtroBanco", "PlazoMaximoCobroMismoBanco", "PlazoMaximoCobroOtroBanco",
						"IdPais", "NombrePais", "IdCiudad", "NombreCiudad", "IdEstado"							    
				From "BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza"
				Order by "NombrePlaza";

		
		Arr_IdPlaza						:= ARRAY_AGG(:v_DatoTablaPlaza."IdPlaza");
		Arr_Codigo						:= ARRAY_AGG(:v_DatoTablaPlaza."Codigo");
		Arr_NombrePlaza					:= ARRAY_AGG(:v_DatoTablaPlaza."NombrePlaza");
		Arr_PlazoRetencionMismoBanco	:= ARRAY_AGG(:v_DatoTablaPlaza."PlazoRetencionMismoBanco");
		Arr_PlazoRetencionOtroBanco		:= ARRAY_AGG(:v_DatoTablaPlaza."PlazoRetencionOtroBanco");
		Arr_PlazoMaximoCobroMismoBanco	:= ARRAY_AGG(:v_DatoTablaPlaza."PlazoMaximoCobroMismoBanco");
		Arr_PlazoMaximoCobroOtroBanco	:= ARRAY_AGG(:v_DatoTablaPlaza."PlazoMaximoCobroOtroBanco");
		Arr_IdPais						:= ARRAY_AGG(:v_DatoTablaPlaza."IdPais");
		Arr_NombrePais					:= ARRAY_AGG(:v_DatoTablaPlaza."NombrePais");
		Arr_IdEstado					:= ARRAY_AGG(:v_DatoTablaPlaza."IdEstado");
		Arr_IdCiudad					:= ARRAY_AGG(:v_DatoTablaPlaza."IdCiudad");
		Arr_NombreCiudad				:= ARRAY_AGG(:v_DatoTablaPlaza."NombreCiudad");


		FOR v_a IN 1 .. CARDINALITY(:Arr_IdPlaza) DO
			v_Arr_IdPlaza					:= :Arr_IdPlaza[:v_a];
			v_Arr_Codigo					:= :Arr_Codigo[:v_a];
			v_Arr_NombrePlaza				:= :Arr_NombrePlaza[:v_a];
			v_Arr_PlazoRetencionMismoBanco	:= :Arr_PlazoRetencionMismoBanco[:v_a];
			v_Arr_PlazoRetencionOtroBanco	:= :Arr_PlazoRetencionOtroBanco[:v_a];
			v_Arr_PlazoMaximoCobroMismoBanco:= :Arr_PlazoMaximoCobroMismoBanco[:v_a];
			v_Arr_PlazoMaximoCobroOtroBanco	:= :Arr_PlazoMaximoCobroOtroBanco[:v_a];
			v_Arr_IdPais					:= :Arr_IdPais[:v_a];
			v_Arr_NombrePais				:= :Arr_NombrePais[:v_a];
			v_Arr_IdEstado					:= :Arr_IdEstado[:v_a];
			v_Arr_IdCiudad					:= :Arr_IdCiudad[:v_a];
			v_Arr_NombreCiudad				:= :Arr_NombreCiudad[:v_a];
			
			
			
			v_Dato = 	
					Select v_a As "NumFila", 'IdPlaza' As "Campo", CAST(v_Arr_IdPlaza As nvarchar(40)) As "Valor"
                	From dummy union all				
					Select v_a As "NumFila", 'Codigo' As "Campo", v_Arr_Codigo As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'NombrePlaza' As "Campo", v_Arr_NombrePlaza As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'PlazoRetencionMismoBanco' As "Campo", CAST(v_Arr_PlazoRetencionMismoBanco As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'PlazoRetencionOtroBanco' As "Campo", CAST(v_Arr_PlazoRetencionOtroBanco As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'PlazoMaximoCobroMismoBanco' As "Campo", CAST(v_Arr_PlazoMaximoCobroMismoBanco As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'PlazoMaximoCobroOtroBanco' As "Campo", CAST(v_Arr_PlazoMaximoCobroOtroBanco As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'IdPais' As "Campo", CAST(v_Arr_IdPais As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'NombrePais' As "Campo", v_Arr_NombrePais As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'IdEstado' As "Campo", v_Arr_IdEstado As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'IdCiudad' As "Campo", CAST(v_Arr_IdCiudad As nvarchar(40)) As "Valor"
               		From dummy union all				
					Select v_a As "NumFila", 'NombreCiudad' As "Campo", v_Arr_NombreCiudad As "Valor"
               		From dummy union all				
			    
	                Select "NumFila", "Campo", "Valor"
	                From :v_Dato
	                Order by "NumFila";
              
		END FOR;										
 	END IF; -- IF UPPER(:p_Mantenedor) = 'PLAZA'




	-- MANTENEDOR ESPECIES EN GARANTIA
    IF UPPER(:p_Mantenedor) = 'ESPGTA' THEN
              v_DatoTablaEspecies = Select especies."IdEspecieGarantia", especies."Codigo", especies."Descripcion", especies."IdEstado"
              						From "BSCL.Custodia.BD::GTA.EspecieEnGarantia" especies;
   
              Arr_IdEspecie := ARRAY_AGG(:v_DatoTablaEspecies."IdEspecieGarantia");
              Arr_CodigoEspecie := ARRAY_AGG(:v_DatoTablaEspecies."Codigo");
              Arr_DescEspecie := ARRAY_AGG(:v_DatoTablaEspecies."Descripcion");
              Arr_IdEstadoEspecie := ARRAY_AGG(:v_DatoTablaEspecies."IdEstado");
              
              FOR v_a IN 1 .. CARDINALITY(:Arr_IdEspecie) DO
                  v_Arr_IdEspecie := :Arr_IdEspecie[:v_a]; 
                  v_Arr_CodigoEspecie := :Arr_CodigoEspecie[:v_a];
                  v_Arr_DescEspecie := :Arr_DescEspecie[:v_a];
                  v_Arr_IdEstadoEspecie := :Arr_IdEstadoEspecie[:v_a];
                  
                  v_Dato = Select v_a As "NumFila", 'IdEspecieGarantia' As "Campo", CAST(v_Arr_IdEspecie as NVARCHAR(40)) As "Valor"
                           From dummy union all
                           Select v_a As "NumFila", 'Codigo' As "Campo", CAST(v_Arr_CodigoEspecie as NVARCHAR(6)) As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'Descripcion' As "Campo", CAST(v_Arr_DescEspecie as NVARCHAR(100)) As "Valor"
                           From dummy union all                                                
                           Select v_a As "NumFila", 'IdEstado' As "Campo", CAST(v_Arr_IdEstadoEspecie as NVARCHAR(3)) AS "Valor"
                           From dummy union all
                           
                           Select "NumFila", "Campo", "Valor"
                           From :v_Dato
                           Order by "NumFila";
              END FOR;	
	END IF;  

	-- MANTENEDOR DE PARAMETROS DE CONFIGURACION APV
    IF UPPER(:p_Mantenedor) = 'CONAPV' THEN
        v_DatoTablaConfig = Select CONFIGURACION."IdEmpresa", CONFIGURACION."CodigoParametro", 
                                   CONFIGURACION."Descripcion", CONFIGURACION."Valor"
                            From "BSCL.APV.BD::PLA.ConfiguracionAPV" CONFIGURACION WHERE "IdEmpresa" = p_IdEmpresa; 
        
        Arr_IdEmpresaDato := ARRAY_AGG(:v_DatoTablaConfig."IdEmpresa");          
        Arr_CodigoParametroDato := ARRAY_AGG(:v_DatoTablaConfig."CodigoParametro");          
        Arr_DescripcionDato := ARRAY_AGG(:v_DatoTablaConfig."Descripcion");
        Arr_ValorDato := ARRAY_AGG(:v_DatoTablaConfig."Valor"); 
        FOR v_a IN 1 .. CARDINALITY(:Arr_CodigoParametroDato) DO
          v_Arr_IdEmpresaDato := :Arr_IdEmpresaDato[:v_a];
          v_Arr_CodigoParametroDato := :Arr_CodigoParametroDato[:v_a];
          v_Arr_DescripcionDato := :Arr_DescripcionDato[:v_a];
          v_Arr_ValorDato := :Arr_ValorDato[:v_a];              
          
          v_Dato = Select v_a As "NumFila", 'IdEmpresa' As "Campo",  CAST(v_Arr_IdEmpresaDato As NVARCHAR(100)) As "Valor"
                   From dummy union all
                   Select v_a As "NumFila", 'CodigoParametro' As "Campo", v_Arr_CodigoParametroDato As "Valor"
                   From dummy union all
                   Select v_a As "NumFila", 'Descripcion' As "Campo", v_Arr_DescripcionDato As "Valor"
                   From dummy union all 
                   Select v_a As "NumFila", 'Valor' As "Campo", v_Arr_ValorDato As "Valor"
                   From dummy union all                       
                   Select "NumFila", "Campo", "Valor"
                   From :v_Dato
                   Order by "NumFila";                       
        END FOR;
	END IF;

	

	-- MANTENEDOR DE DERIVADOS
    IF UPPER(:p_Mantenedor) = 'DERIVA' THEN
    	
         v_DatoTablaConf = 	SELECT CONF."IdEmpresa"
         						, CONF."ModalidadEjercicio"
         						, MODAL."NombreParametro" 	AS "ModalidadEjercicioDesc"
         						, CONF."TipoLiquidacion"
         						, LIQ."NombreParametro"		AS  "TipoLiquidacionDesc"
         						, CONF."ParametroEjercicio"
         						, CONFPL."NombreParametro" 	AS "ParametroEjercicioDesc"
         						, CONF."ParametroEjercicioValor"
         						
                  			FROM "BSCL.Custodia.BD::DER.Configuracion" CONF
                    		LEFT JOIN "BSCL.Plataforma.BD::DER.Configuracion" CONFPL 
                      				ON CONFPL."ValorParametro" = CONF."ParametroEjercicio" AND CONFPL."NombreTipoParametro" = 'ParametroEjercicio'
                      		LEFT JOIN "BSCL.Plataforma.BD::DER.Configuracion" MODAL 
                      				ON MODAL."ValorParametro" = CONF."ModalidadEjercicio"  AND MODAL."NombreTipoParametro" = 'ModalidadEjercicio'
                      		LEFT JOIN "BSCL.Plataforma.BD::DER.Configuracion" LIQ 
                      				ON LIQ."ValorParametro" = CONF."TipoLiquidacion" AND LIQ."NombreTipoParametro" = 'ModalidadOpcion';
                      
          Arr_IdEmpresaDato := ARRAY_AGG(:v_DatoTablaConf."IdEmpresa");          
          Arr_ModalidadEjercicioDato := ARRAY_AGG(:v_DatoTablaConf."ModalidadEjercicio");          
          Arr_ModalidadEjercicioDescDato := ARRAY_AGG(:v_DatoTablaConf."ModalidadEjercicioDesc");
          Arr_TipoLiquidacionDato := ARRAY_AGG(:v_DatoTablaConf."TipoLiquidacion");
          Arr_TipoLiquidacionDescDato := ARRAY_AGG(:v_DatoTablaConf."TipoLiquidacionDesc");
          Arr_ParametroEjercicioDato := ARRAY_AGG(:v_DatoTablaConf."ParametroEjercicio");
          Arr_ParametroEjercicioDescDato := ARRAY_AGG(:v_DatoTablaConf."ParametroEjercicioDesc");
          Arr_ParametroEjercicioValor := ARRAY_AGG(:v_DatoTablaConf."ParametroEjercicioValor");
         
          FOR v_a IN 1 .. CARDINALITY(:Arr_IdEmpresaDato) DO
              v_Arr_IdEmpresaDato := :Arr_IdEmpresaDato[:v_a];
              v_Arr_ModalidadEjercicioDato := :Arr_ModalidadEjercicioDato[:v_a];
              v_Arr_ModalidadEjercicioDescDato := :Arr_ModalidadEjercicioDescDato[:v_a];
              v_Arr_TipoLiquidacionDato := :Arr_TipoLiquidacionDato[:v_a];
              v_Arr_TipoLiquidacionDescDato := :Arr_TipoLiquidacionDescDato[:v_a];
              v_Arr_ParametroEjercicioDato := :Arr_ParametroEjercicioDato[:v_a];
              v_Arr_ParametroEjercicioDescDato := :Arr_ParametroEjercicioDescDato[:v_a];
              v_Arr_ParametroEjercicioValor := :Arr_ParametroEjercicioValor[:v_a];         

              v_Dato = Select v_a As "NumFila", 'IdEmpresa' As "Campo", CAST(v_Arr_IdEmpresaDato As nvarchar(100))  As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'ModalidadEjercicio' As "Campo", v_Arr_ModalidadEjercicioDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'ModalidadEjercicioDesc' As "Campo", v_Arr_ModalidadEjercicioDescDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'TipoLiquidacion' As "Campo", v_Arr_TipoLiquidacionDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'TipoLiquidacionDesc' As "Campo", v_Arr_TipoLiquidacionDescDato As "Valor"
                       From dummy union all 
                       Select v_a As "NumFila", 'ParametroEjercicio' As "Campo", v_Arr_ParametroEjercicioDato As "Valor"
                       From dummy union all                               
                       Select v_a As "NumFila", 'ParametroEjercicioDesc' As "Campo", v_Arr_ParametroEjercicioDescDato As "Valor"
                       From dummy union all                               
                       Select v_a As "NumFila", 'ParametroEjercicioValor' As "Campo", v_Arr_ParametroEjercicioValor As "Valor"    
                       From dummy union all                               
                       Select "NumFila", "Campo", "Valor"
                       From :v_Dato
                       Order by "NumFila";
          END FOR;
	END IF;   

	-- MANTENEDOR CODIGO TIPO RELACION
    IF UPPER(:p_Mantenedor) = 'TIPREL' THEN
    	
         v_DatoTablaTRel = 	SELECT CONF."IdEmpresa"
         						, CONF."CodigoTipoRelacion"
         						, CONF."DescripcionTipoRelacion"
         						
                  			FROM "BSCL.Plataforma.BD::REL.CodigoTipoRelacion" CONF;
                      
          Arr_IdEmpresaDato := ARRAY_AGG(:v_DatoTablaTRel."IdEmpresa");          
          Arr_CodigoTipoRelacionDato := ARRAY_AGG(:v_DatoTablaTRel."CodigoTipoRelacion");          
          Arr_DescripcionTipoRelacionDato := ARRAY_AGG(:v_DatoTablaTRel."DescripcionTipoRelacion");
       
          FOR v_a IN 1 .. CARDINALITY(:Arr_IdEmpresaDato) DO
              v_Arr_IdEmpresaDato := :Arr_IdEmpresaDato[:v_a];
              v_Arr_CodigoTipoRelacionDato := :Arr_CodigoTipoRelacionDato[:v_a];
              v_Arr_DescripcionTipoRelacionDato := :Arr_DescripcionTipoRelacionDato[:v_a];

              v_Dato = Select v_a As "NumFila", 'IdEmpresa' As "Campo", CAST(v_Arr_IdEmpresaDato As nvarchar(100))  As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'CodigoTipoRelacion' As "Campo", v_Arr_CodigoTipoRelacionDato As "Valor"
                       From dummy union all
                       Select v_a As "NumFila", 'DescripcionTipoRelacion' As "Campo", v_Arr_DescripcionTipoRelacionDato As "Valor"
                       From dummy union all                               
                       Select "NumFila", "Campo", "Valor"
                       From :v_Dato
                       Order by "NumFila";
          END FOR;
	END IF; 
END;
