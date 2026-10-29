package cl.deyfer.tcg;

import cl.deyfer.tcg.config.Data;
import cl.deyfer.tcg.repository.ProductoTcgRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataTest {

    @Mock
    private ProductoTcgRepository repository;

    @InjectMocks
    private Data data;

    @Test
    void run_cuandoBDVacia_debeInsertarTodosLosProductos() {
        when(repository.count()).thenReturn(0L);
        when(repository.saveAll(anyList())).thenReturn(Collections.emptyList());

        data.run();

        verify(repository, times(1)).count();
        verify(repository, times(1)).saveAll(anyList());
    }

    @Test
    void run_cuandoBDYaTieneDatos_noDebeInsertar() {
        when(repository.count()).thenReturn(10L);

        data.run();

        verify(repository, times(1)).count();
        verify(repository, never()).saveAll(anyList());
    }
}