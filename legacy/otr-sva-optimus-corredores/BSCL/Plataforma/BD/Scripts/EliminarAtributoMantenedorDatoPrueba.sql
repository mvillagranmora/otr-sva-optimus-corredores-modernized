SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDatoPrueba";

CREATE PROCEDURE
   "BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDatoPrueba"
 (
    IN p_Mantenedor NVARCHAR(6),    
    
    --OUT p_OUT_ID              BIGINT,   
    OUT p_out_ErroresContador BIGINT
  )
As
Begin

  Declare xx table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520));

  --Moneda
  /*xx = Select 1 As "NumFila", 'IdMoneda' As "Campo", 'PRU' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'Codigo' As "Campo", 'PRUEBA' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'CodigoNumerico' As "Campo", '999' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'Nombre' As "Campo", 'PRUEBAPRUEBA' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'Simbolo' As "Campo", 'PPP' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'Decimales' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'MultiplicaODivide' As "Campo", '' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'RangoSuperiorParidad' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'RangoInferiorParidad' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'PorcentajeSuperiorParidad' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'PorcentajeInferiorParidad' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'RangoSuperiorTipoCambio' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'RangoInferiorTipoCambio' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'PorcentajeSuperiorTipoCambio' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'PorcentajeInferiorTipoCambio' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'Orden' As "Campo", '0' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'IdPais' As "Campo", '1' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'IdEstado' As "Campo", 'ACT' As "Valor" From Dummy; */ 
  
  --Moneda
       xx = Select 1 As "NumFila", 'IdMoneda' As "Campo", 'YEN' As "Valor" From Dummy;
      
       
  --Configuracion 
  /*xx = Select 1 As "NumFila", 'IdEmpresa' As "Campo", '1' As "Valor" From Dummy UNION ALL
       Select 1 As "NumFila", 'CodigoParametro' As "Campo", 'PRUEBA' As "Valor" From Dummy;*/
      
  
  call  "BSCL.Plataforma.BD.Servicios::EliminarAtributoMantenedorDato" (:p_Mantenedor, :xx, p_out_ErroresContador);
  
End;
