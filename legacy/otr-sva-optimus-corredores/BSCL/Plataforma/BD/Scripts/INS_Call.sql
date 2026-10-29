/*========================================================================*/
/* Proc:                                                                  */
/*========================================================================*/
call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarPrecioInstrumento"
			(p_IdEmpresa=>1
			,p_IdClasificacionTipoInstrumento => 'RV'
			,p_TipoInstrumento 			=>'ACN'
			,p_NemoTecnico     			=>'LAN'
			,p_Fecha           			=>'2015-01-01'
			,p_IdModulo        			=>'ORD'
			,p_IdTipoOperacion 			=>'COMSIM'			
			,p_UsoPrecio				=>'INGORDSIM'
			,p_IdConcepto  				=>''
			,p_out_Fecha       			=>?
			,p_out_Hora        			=>?
			,p_out_Fuente	 			=>?
			,p_out_TipoPrecio 			=>?
			,p_out_IdMoneda 			=>?
			,p_out_SecuenciaLista		=>?
			,p_out_Precio_Cierre 		=>?
			,p_out_Precio_Mayor 		=>?
			,p_out_Precio_Menor			=>?
			,p_out_UltimoPrecioTransado	=>?
			,p_out_UltimoPrecioCompra	=>?
			,p_out_UltimoPrecioVenta	=>?
			);

			
select * from "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio"