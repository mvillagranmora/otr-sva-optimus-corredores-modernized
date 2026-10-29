SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDato";

CREATE PROCEDURE
-- 
--  Proposito: Eliminar registro de mantenedor dinamico
--  Autor: Natalia Silva Muñoz
--  Fecha Creacion: 13/11/2015 
--
"BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDato"
  (
    IN p_IdEmpresa      			BIGINT,
  	IN p_IdUsuario      			BIGINT,
    IN p_Mantenedor NVARCHAR(6),    
    IN p_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520)),
    
    OUT p_OUT_ID NVARCHAR(1000),
    --Salidas para mensajes de advertencias y errores
	--OUT p_out_AdvertenciasContador INT,
	--OUT p_out_AdvertenciasDetalle  TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador      INT
	--OUT p_out_ErroresDetalle       TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
  )
As
Begin
   Declare v_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520));
     
   --Moneda
   Declare ValorCodigo, ValorSimbolo, ValorMultiplicaODivide NVARCHAR(40);
   Declare ValorCodigoNumerico, ValorDecimales, ValorOrden Integer;
   Declare ValorNombre NVARCHAR(100);
   Declare ValorRangoSuperiorParidad, ValorRangoInferiorParidad, ValorPorcentajeSuperiorParidad, ValorPorcentajeInferiorParidad,
           ValorRangoSuperiorTipoCambio, ValorRangoInferiorTipoCambio, ValorPorcentajeSuperiorTipoCambio,
           ValorPorcentajeInferiorTipoCambio  Decimal(26,8);
   Declare ValorIdPais BIGINT;  
   Declare ValorIdMoneda, ValorIdEstado NVARCHAR(3);   
   
   --Configuracion
   Declare ValorIdEmpresa BIGINT;
   Declare ValorCodigoParametro NVARCHAR(40);
   Declare ValorDescripcion, ValorValor NVARCHAR(520);
   DECLARE ModalidadEjercicio,TipoLiquidacion,ParametroEjercicio,ParametroEjercicioValor NVARCHAR (100);
   DECLARE CodigoTipoRelacion nvarchar(10);
   
   --Proposito
   Declare ValorIdPropositoPortafolio NVARCHAR(40);
   
   --EspecieEnGarantia
	Declare ValorIdEspecieGarantia BIGINT;
	
	--Mercado
   Declare ValorIdMercado NVARCHAR(4);
   
   v_Dato = Select "NumFila", "Campo", "Valor" from :p_Dato;
            
    
	If UPPER(:p_Mantenedor) = 'MONEDA' Then 
		Select "Valor" Into ValorIdMoneda From :v_Dato Where "Campo" = 'IdMoneda';                              
      
		Update "BSCL.Plataforma.BD::PLA.Moneda" 
		Set "IdEstado" = 'INA'
		Where "IdMoneda" = ValorIdMoneda;
	End If;
   
   
	If UPPER(:p_Mantenedor) = 'PROPOS' Then 
    	Select "Valor" Into ValorIdPropositoPortafolio From :v_Dato Where "Campo" = 'IdPropositoPortafolio';                              
      
		Update "BSCL.Portafolio.BD::POR.PropositoPortafolio" 
		Set "IdEstado" = 'INA'
		Where "IdPropositoPortafolio" = ValorIdPropositoPortafolio;
	End if;
   
   
	If UPPER(:p_Mantenedor) = 'ESPGTA' Then 
    	Select CAST("Valor" as BIGINT) Into ValorIdEspecieGarantia From :v_Dato Where "Campo" = 'IdEspecieGarantia';                              
      
		Update "BSCL.Custodia.BD::GTA.EspecieEnGarantia" 
		Set "IdEstado" = 'INA'
		Where "IdEspecieGarantia" = ValorIdEspecieGarantia;
	End if;
	
	If UPPER(:p_Mantenedor) = 'MERCAD' Then 
    	Select "Valor" Into ValorIdMercado From :v_Dato Where "Campo" = 'IdMercado';                              
      
		Update "BSCL.Plataforma.BD::INS.Mercado" 
		Set "IdEstado" = 'INA'
		Where "IdMercado" = ValorIdMercado;
	End if;

	If UPPER(:p_Mantenedor) = 'DERIVA' THEN
		SELECT "Valor" Into ModalidadEjercicio From :v_Dato Where "Campo" = 'ModalidadEjercicio';
		SELECT "Valor" Into TipoLiquidacion From :v_Dato Where "Campo" = 'TipoLiquidacion';
		SELECT "Valor" Into ParametroEjercicio From :v_Dato Where "Campo" = 'ParametroEjercicio';
		SELECT "Valor" Into ParametroEjercicioValor From :v_Dato Where "Campo" = 'ParametroEjercicioValor';
		
    	--SELECT * FROM "BSCL.Custodia.BD::DER.Configuracion" 
		DELETE FROM "BSCL.Custodia.BD::DER.Configuracion"
		WHERE 	"IdEmpresa" 				= :p_IdEmpresa
			AND "ModalidadEjercicio" 		= ModalidadEjercicio
			AND "TipoLiquidacion"			= TipoLiquidacion
			AND "ParametroEjercicio" 		= ParametroEjercicio
			AND "ParametroEjercicioValor" 	= ParametroEjercicioValor;
	End if;
	
	If UPPER(:p_Mantenedor) = 'TIPREL' THEN
		SELECT "Valor" Into CodigoTipoRelacion From :v_Dato Where "Campo" = 'CodigoTipoRelacion';
		
		DELETE FROM "BSCL.Plataforma.BD::REL.CodigoTipoRelacion"
		WHERE 	"IdEmpresa" 				= :p_IdEmpresa
			AND "CodigoTipoRelacion" 		= CodigoTipoRelacion;
	End if;
   
   p_OUT_ID := 0;
   p_out_ErroresContador := 0;
                           
End;
