import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import Chart from 'chart.js/auto';

import { TabViewModule } from 'primeng/tabview';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ChartModule } from 'primeng/chart';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { DialogModule } from 'primeng/dialog';

import { PraeService, QtdeEstagiosOrientadorDto, EstatisticasSemestreDto, TiposEstagiosPorSemestreDto, EmpresaComSupervisoresDto, SupervisorDto, EstatisticasDuracaoEstagioDto } from '../../services/prae.service';

@Component({
  selector: 'app-prae',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TabViewModule,
    ButtonModule,
    TableModule,
    ChartModule,
    DropdownModule,
    MultiSelectModule,
    DialogModule
  ],
  templateUrl: './prae.component.html',
  styleUrl: './prae.component.scss'
})
export class PraeComponent implements OnInit, AfterViewInit {
  tabelaObrigatorios: any[] = [];
  colunasObrigatorios: any[] = [];
  tabelaNaoObrigatorios: any[] = [];
  colunasNaoObrigatorios: any[] = [];

  chartDataObrigatorios: any;
  chartOptionsObrigatorios: any;
  chartDataNaoObrigatorios: any;
  chartOptionsNaoObrigatorios: any;

  chartDataTiposEstagios: any;
  chartOptionsTiposEstagios: any;
  tiposEstagiosPorSemestre: TiposEstagiosPorSemestreDto[] = [];
  isLoadingTiposEstagios = false;

  chartDataEstagiosPorMes: any;
  chartOptionsEstagiosPorMes: any;
  chartDataEstagiosPorMesObrigatorio: any;
  chartOptionsEstagiosPorMesObrigatorio: any;
  chartDataEstagiosPorMesNaoObrigatorio: any;
  chartOptionsEstagiosPorMesNaoObrigatorio: any;
  estadoEstagiosPorMes: any[] = [];
  estadoEstagiosPorMesFiltrados: any[] = [];
  isLoadingEstagiosPorMes = false;

  mesInicial: number = 1;
  anoInicial: number = new Date().getFullYear();
  mesFinal: number = 12;
  anoFinal: number = new Date().getFullYear();
  listaAnosMeses: any[] = [];

  isLoading = false;
  errorMessage = '';

  estatisticasOrientadores: QtdeEstagiosOrientadorDto[] = [];
  estatisticasFiltradas: QtdeEstagiosOrientadorDto[] = [];

  orientadoresSelecionados: string[] = [];
  listaOrientadores: any[] = [];
  buscaOrientador: string = '';

  semestreInicial: string = '';
  semestreFinal: string = '';
  listaSemestres: any[] = [];

  displayOrientadoresDialog: boolean = false;

  empresasComSupervisores: any[] = [];
  empresasComSupervisoresFiltradas: any[] = [];
  isLoadingSupervisores = false;
  errorMessageSupervisores = '';

  empresaFiltro: string = '';
  listaEmpresas: any[] = [];

  chartDataEmpresasEstagios: any;
  chartOptionsEmpresasEstagios: any;
  empresasEstagiosFiltradas: any[] = [];
  empresasEstagiosParaGrafico: any[] = [];
  filtroGraficoEmpresas: string = '';
  mostrarTop10Empresas = true;

  semestreGraficoInicial: string = '';
  semestreGraficoFinal: string = '';
  listaSemestresGrafico: any[] = [];

  expandedRows: { [key: string]: boolean } = {};

  estatisticasDuracaoPorTipo: any = null;
  estatisticasDuracaoPorTipoEstagio: any = null;
  chartBoxPlotObrigatorio: any = null;
  chartBoxPlotNaoObrigatorio: any = null;
  isLoadingDuracao = false;
  errorMessageDuracao = '';
  boxplotPluginRegistered = false;

  tiposEstagio: string[] = [];
  listaTiposEstagio: any[] = [];
  estatisticasCurrentes: any = null;

  @ViewChild('boxplotChartObrigatorio') boxplotCanvasRefObrigatorio!: ElementRef<HTMLCanvasElement>;
  @ViewChild('boxplotChartNaoObrigatorio') boxplotCanvasRefNaoObrigatorio!: ElementRef<HTMLCanvasElement>;

  get listaOrientadoresFiltrados() {
    if (!this.buscaOrientador || this.buscaOrientador.trim() === '') {
      return this.listaOrientadores;
    }
    const busca = this.buscaOrientador.toLowerCase();
    return this.listaOrientadores.filter(o =>
      o.label.toLowerCase().includes(busca)
    );
  }

  constructor(
    private praeService: PraeService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    this.initializeChartOptionsGraficosArea();
    this.initializeChartOptionsTiposEstagios();
    this.initializeChartOptionsEstagiosPorMes();
    this.loadEstatisticas();
    this.loadSupervisores();
  }

  ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      import('@sgratzl/chartjs-chart-boxplot').then((module: any) => {
        try {
          const BoxPlotController = module.BoxPlotController || module.default?.BoxPlotController;
          const ViolinController = module.ViolinController || module.default?.ViolinController;
          const BoxAndWiskers = module.BoxAndWiskers || module.default?.BoxAndWiskers;
          const Violin = module.Violin || module.default?.Violin;

          if (BoxPlotController && ViolinController && BoxAndWiskers && Violin) {
            Chart.register(BoxPlotController, ViolinController, BoxAndWiskers, Violin);
            this.boxplotPluginRegistered = true;
          } else {
            this.boxplotPluginRegistered = false;
          }
        } catch (error) {
          console.error('Erro ao registrar gráfico box plot:', error);
          this.boxplotPluginRegistered = false;
        }

        setTimeout(() => {
          this.loadEstatisticasDuracao();
        }, 50);
      }).catch((error: any) => {
        console.error('Erro ao importar módulo box plot:', error);
        this.loadEstatisticasDuracao();
      });
    }
  }

  /**
   * Configura as opções visuais dos gráficos de área
   */
  private initializeChartOptionsGraficosArea() {
    const commonOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: {
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            }
          }
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const label = context.dataset.label || '';
              const value = context.parsed.y;
              return `${label}: ${value} estágio(s)`;
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Semestre',
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 11
            }
          },
          grid: {
            color: '#e9ecef'
          }
        },
        y: {
          beginAtZero: true,
          suggestedMin: 0,
          title: {
            display: true,
            text: 'Quantidade de Estágios',
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 11
            },
            stepSize: 5
          },
          grid: {
            color: '#e9ecef'
          }
        }
      }
    };

    this.chartOptionsObrigatorios = {
      ...commonOptions,
      plugins: {
        ...commonOptions.plugins,
        title: {
          display: true,
          text: 'Estágios Obrigatórios por Orientador e Semestre',
          color: '#28a745',
          font: {
            size: 16,
            weight: 'bold'
          }
        }
      }
    };

    this.chartOptionsNaoObrigatorios = {
      ...commonOptions,
      plugins: {
        ...commonOptions.plugins,
        title: {
          display: true,
          text: 'Estágios Não Obrigatórios por Orientador e Semestre',
          color: '#dc3545',
          font: {
            size: 16,
            weight: 'bold'
          }
        }
      }
    };
  }

  /**
   * Configura as opções visuais do gráfico de tipos de estágios
   */
  private initializeChartOptionsTiposEstagios() {
    this.chartOptionsTiposEstagios = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          labels: {
            color: '#071D41',
            font: {
              size: 14,
              weight: 'bold'
            }
          }
        },
        title: {
          display: true,
          text: 'Tipos de Estágios por Semestre',
          color: '#071D41',
          font: {
            size: 18,
            weight: 'bold'
          }
        },
        tooltip: {
          callbacks: {
            label: (context: any) => {
              const label = context.dataset.label || '';
              const value = context.parsed.y;
              return `${label}: ${value} estágio(s)`;
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Semestre',
            color: '#071D41',
            font: {
              size: 14,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 12
            }
          },
          grid: {
            color: '#e9ecef'
          }
        },
        y: {
          beginAtZero: true,
          suggestedMin: 0,
          suggestedMax: 50,
          title: {
            display: true,
            text: 'Quantidade de Estágios',
            color: '#071D41',
            font: {
              size: 14,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 12
            },
            stepSize: 5
          },
          grid: {
            color: '#e9ecef'
          }
        }
      }
    };
  }

  /**
   * Configura as opções visuais do gráfico de estágios por mês
   */
  private initializeChartOptionsEstagiosPorMes() {
    this.chartOptionsEstagiosPorMes = {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false
      },
      plugins: {
        legend: {
          position: 'top',
          labels: {
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            },
            padding: 15,
            usePointStyle: true
          }
        },
        title: {
          display: true,
          text: 'Estágios Iniciados e Finalizados por Mês',
          color: '#071D41',
          font: {
            size: 16,
            weight: 'bold'
          },
          padding: 20
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          titleColor: '#fff',
          bodyColor: '#fff',
          borderColor: '#ddd',
          borderWidth: 1,
          callbacks: {
            label: (context: any) => {
              const label = context.dataset.label || '';
              const value = context.parsed.y;
              return `${label}: ${value} estágio(s)`;
            }
          }
        }
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'Período (Mês/Ano)',
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 11
            }
          },
          grid: {
            color: '#e9ecef',
            drawBorder: true
          }
        },
        y: {
          beginAtZero: true,
          suggestedMin: 0,
          title: {
            display: true,
            text: 'Quantidade de Estágios',
            color: '#071D41',
            font: {
              size: 12,
              weight: 'bold'
            }
          },
          ticks: {
            color: '#071D41',
            font: {
              size: 11
            },
            stepSize: 5
          },
          grid: {
            color: '#e9ecef',
            drawBorder: true
          }
        }
      }
    };
  }

  /**
   * Carrega as estatísticas do backend
   */
  loadEstatisticas() {
    this.isLoading = true;
    this.errorMessage = '';

    this.praeService.getEstagiosPorOrientador().subscribe({
      next: (data) => {
        this.estatisticasOrientadores = data;
        this.prepareOrientadoresList();
        this.prepareSemestresList();
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar estatísticas:', error);
        this.errorMessage = 'Erro ao carregar dados. Tente novamente mais tarde.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Prepara os dados para as tabelas de orientações por semestre
   */
  private prepareTableData() {
    if (!this.estatisticasFiltradas || this.estatisticasFiltradas.length === 0) {
      this.tabelaObrigatorios = [];
      this.colunasObrigatorios = [];
      this.tabelaNaoObrigatorios = [];
      this.colunasNaoObrigatorios = [];
      return;
    }

    const semestresSet = new Set<string>();
    this.estatisticasFiltradas.forEach(est => {
      if (est.estatisticasPorSemestre && est.estatisticasPorSemestre.length > 0) {
        est.estatisticasPorSemestre.forEach(sem => {
          semestresSet.add(`${sem.ano}/${sem.semestre}`);
        });
      }
    });

    let semestres = Array.from(semestresSet).sort((a, b) => {
      const [anoA, semA] = a.split('/').map(Number);
      const [anoB, semB] = b.split('/').map(Number);
      if (anoA !== anoB) return anoA - anoB;
      return semA - semB;
    });

    if (this.semestreInicial && this.semestreFinal) {
      semestres = semestres.filter(semestre => {
        const [anoIni, semIni] = this.semestreInicial.split('/').map(Number);
        const [anoFim, semFim] = this.semestreFinal.split('/').map(Number);
        const [ano, sem] = semestre.split('/').map(Number);

        const valorIni = anoIni * 10 + semIni;
        const valorFim = anoFim * 10 + semFim;
        const valorAtual = ano * 10 + sem;

        return valorAtual >= valorIni && valorAtual <= valorFim;
      });
    }

    if (semestres.length === 0) {
      this.tabelaObrigatorios = [];
      this.colunasObrigatorios = [];
      this.tabelaNaoObrigatorios = [];
      this.colunasNaoObrigatorios = [];
      return;
    }

    this.colunasObrigatorios = [
      { field: 'orientador', header: 'Orientador' }
    ];
    semestres.forEach(semestre => {
      this.colunasObrigatorios.push({ field: semestre, header: semestre });
    });

    this.colunasNaoObrigatorios = [
      { field: 'orientador', header: 'Orientador' }
    ];
    semestres.forEach(semestre => {
      this.colunasNaoObrigatorios.push({ field: semestre, header: semestre });
    });

    this.tabelaObrigatorios = this.estatisticasFiltradas.map(est => {
      const linha: any = {
        orientador: est.nomeOrientador
      };

      semestres.forEach(semestre => {
        const [ano, sem] = semestre.split('/').map(Number);
        const estatSemestre = est.estatisticasPorSemestre?.find(
          s => s.ano === ano && s.semestre === sem
        );

        const qtd = estatSemestre ? estatSemestre.quantidadeObrigatorios : 0;
        linha[semestre] = qtd;
      });

      return linha;
    });

    this.tabelaNaoObrigatorios = this.estatisticasFiltradas.map(est => {
      const linha: any = {
        orientador: est.nomeOrientador
      };

      semestres.forEach(semestre => {
        const [ano, sem] = semestre.split('/').map(Number);
        const estatSemestre = est.estatisticasPorSemestre?.find(
          s => s.ano === ano && s.semestre === sem
        );

        const qtd = estatSemestre ? estatSemestre.quantidadeNaoObrigatorios : 0;
        linha[semestre] = qtd;
      });

      return linha;
    });

    this.prepareAreaChartsData();
  }

  /**
   * Prepara os dados para os gráficos de área
   */
  private prepareAreaChartsData() {
    if (this.tabelaObrigatorios.length === 0 && this.tabelaNaoObrigatorios.length === 0) {
      this.chartDataObrigatorios = null;
      this.chartDataNaoObrigatorios = null;
      return;
    }

    const semestres = this.colunasObrigatorios
      .slice(1)
      .map(col => col.field);

    const coresPaleta = [
      '#dc3545', '#28a745', '#FFBE00', '#071D41', '#17a2b8',
      '#6f42c1', '#fd7e14', '#20c997', '#e83e8c', '#6c757d',
      '#ffc107', '#343a40', '#0dcaf0', '#adb5bd', '#d9534f'
    ];

    if (this.tabelaObrigatorios.length > 0) {
      const datasetsObrigatorios = this.tabelaObrigatorios.map((orientador, index) => {
        const data = semestres.map(semestre => orientador[semestre] || 0);
        const cor = coresPaleta[index % coresPaleta.length];

        return {
          label: orientador.orientador,
          data: data,
          borderColor: cor,
          backgroundColor: cor + '40',
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: cor,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 7
        };
      });

      this.chartDataObrigatorios = {
        labels: semestres,
        datasets: datasetsObrigatorios
      };
    }

    if (this.tabelaNaoObrigatorios.length > 0) {
      const datasetsNaoObrigatorios = this.tabelaNaoObrigatorios.map((orientador, index) => {
        const data = semestres.map(semestre => orientador[semestre] || 0);
        const cor = coresPaleta[index % coresPaleta.length];

        return {
          label: orientador.orientador,
          data: data,
          borderColor: cor,
          backgroundColor: cor + '40',
          borderWidth: 2,
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointBackgroundColor: cor,
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointHoverRadius: 7
        };
      });

      this.chartDataNaoObrigatorios = {
        labels: semestres,
        datasets: datasetsNaoObrigatorios
      };
    }
  }

  /**
   * Prepara a lista de orientadores para o filtro
   */
  private prepareOrientadoresList() {
    const orientadoresOrdenados = [...this.estatisticasOrientadores]
      .sort((a, b) => b.totalGeral - a.totalGeral);

    this.listaOrientadores = orientadoresOrdenados.map(est => ({
      label: `${est.nomeOrientador} (${est.totalGeral})`,
      value: est.nomeOrientador
    }));

    this.orientadoresSelecionados = orientadoresOrdenados
      .slice(0, 5)
      .map(o => o.nomeOrientador);
  }

  /**
   * Prepara a lista de semestres para os filtros
   */
  private prepareSemestresList() {
    const semestresSet = new Set<string>();

    this.estatisticasOrientadores.forEach(est => {
      if (est.estatisticasPorSemestre && est.estatisticasPorSemestre.length > 0) {
        est.estatisticasPorSemestre.forEach(sem => {
          semestresSet.add(`${sem.ano}/${sem.semestre}`);
        });
      }
    });

    // Ordena os semestres
    const semestresOrdenados = Array.from(semestresSet).sort((a, b) => {
      const [anoA, semA] = a.split('/').map(Number);
      const [anoB, semB] = b.split('/').map(Number);
      if (anoA !== anoB) return anoA - anoB;
      return semA - semB;
    });

    this.listaSemestres = semestresOrdenados.map(sem => ({
      label: sem,
      value: sem
    }));

    // Define inicial e final
    if (this.listaSemestres.length > 0) {
      this.semestreInicial = this.listaSemestres[0].value;
      this.semestreFinal = this.listaSemestres[this.listaSemestres.length - 1].value;
    }
  }

  /**
   * Aplica os filtros e atualiza o gráfico
   */
  applyFilters() {
    // Filtra orientadores selecionados
    if (this.orientadoresSelecionados.length > 0) {
      this.estatisticasFiltradas = this.estatisticasOrientadores.filter(est =>
        this.orientadoresSelecionados.includes(est.nomeOrientador)
      );
    } else {
      // Se nenhum orientador selecionado, não mostra nada
      this.estatisticasFiltradas = [];
    }

    // Ordena por total (decrescente)
    this.estatisticasFiltradas.sort((a, b) => b.totalGeral - a.totalGeral);

    // Atualiza a tabela
    this.prepareTableData();
  }

  /**
   * Callback quando os filtros mudam
   */
  onFilterChange() {
    this.applyFilters();
  }

  /**
   * Callback quando o semestre inicial muda
   */
  onSemestreInicialChange(event: any) {
    this.semestreInicial = event.value;
    this.applyFilters();
    // Atualiza Tab 2 se já foi carregada
    if (this.tiposEstagiosPorSemestre.length > 0) {
      this.prepareTiposEstagiosChartData();
    }
  }

  /**
   * Callback quando o semestre final muda
   */
  onSemestreFinalChange(event: any) {
    this.semestreFinal = event.value;
    this.applyFilters();
    // Atualiza Tab 2 se já foi carregada
    if (this.tiposEstagiosPorSemestre.length > 0) {
      this.prepareTiposEstagiosChartData();
    }
  }

  /**
   * Verifica se um orientador está selecionado
   */
  isOrientadorSelecionado(nomeOrientador: string): boolean {
    return this.orientadoresSelecionados.includes(nomeOrientador);
  }

  /**
   * Toggle (seleciona/desseleciona) um orientador
   */
  toggleOrientador(nomeOrientador: string) {
    const index = this.orientadoresSelecionados.indexOf(nomeOrientador);
    if (index > -1) {
      // Remove
      this.orientadoresSelecionados.splice(index, 1);
    } else {
      // Adiciona
      this.orientadoresSelecionados.push(nomeOrientador);
    }
    this.applyFilters();
  }

  /**
   * Seleciona todos os orientadores
   */
  selectAll() {
    this.orientadoresSelecionados = this.listaOrientadores.map(o => o.value);
    this.applyFilters();
  }

  /**
   * Limpa a seleção
   */
  clearSelection() {
    this.orientadoresSelecionados = [];
    this.applyFilters();
  }

  /**
   * Limpa todos os filtros
   */
  clearFilters() {
    this.buscaOrientador = '';

    // Reseta período para todos os semestres
    if (this.listaSemestres.length > 0) {
      this.semestreInicial = this.listaSemestres[0].value;
      this.semestreFinal = this.listaSemestres[this.listaSemestres.length - 1].value;
    }

    this.applyFilters();
  }

  /**
   * Detecta mudança de tab
   */
  onTabChange(event: any) {
    // Se mudou para a tab 2 (índice 1) e ainda não carregou os dados
    if (event.index === 1 && this.tiposEstagiosPorSemestre.length === 0) {
      this.loadTiposEstagiosPorSemestre();
    }
    // Se mudou para a tab 3 (índice 2) e ainda não carregou os dados
    if (event.index === 2 && this.estadoEstagiosPorMes.length === 0) {
      this.loadEstagiosPorMes();
    }
  }

  /**
   * Carrega dados de tipos de estágios por semestre (Tab 2)
   */
  loadTiposEstagiosPorSemestre() {
    this.isLoadingTiposEstagios = true;

    this.praeService.getTiposEstagiosPorSemestre().subscribe({
      next: (data) => {
        this.tiposEstagiosPorSemestre = data;
        this.prepareTiposEstagiosChartData();
        this.isLoadingTiposEstagios = false;
      },
      error: (error) => {
        console.error('Erro ao carregar tipos de estágios:', error);
        this.isLoadingTiposEstagios = false;
      }
    });
  }

  /**
   * Carrega dados de estágios iniciados e finalizados por mês (Tab 3)
   */
  loadEstagiosPorMes() {
    this.isLoadingEstagiosPorMes = true;

    this.praeService.getEstagiosIniciadosFinalizadosPorMes().subscribe({
      next: (data) => {
        this.estadoEstagiosPorMes = data;
        this.prepareListaAnosMeses();
        this.applyFiltroEstagiosPorMes();
        this.isLoadingEstagiosPorMes = false;
      },
      error: (error) => {
        console.error('Erro ao carregar estágios por mês:', error);
        this.isLoadingEstagiosPorMes = false;
      }
    });
  }

  /**
   * Prepara a lista de anos e meses disponíveis
   */
  private prepareListaAnosMeses() {
    const anosSet = new Set<number>();
    this.estadoEstagiosPorMes.forEach(item => {
      anosSet.add(item.ano);
    });

    const anosOrdenados = Array.from(anosSet).sort((a, b) => a - b);
    this.listaAnosMeses = anosOrdenados.map(ano => ({ label: ano.toString(), value: ano }));

    // Define ano inicial e final
    if (anosOrdenados.length > 0) {
      this.anoInicial = anosOrdenados[0];
      this.anoFinal = anosOrdenados[anosOrdenados.length - 1];
      this.mesInicial = 1;
      this.mesFinal = 12;
    }
  }

  /**
   * Aplica filtro de período (mês/ano) aos dados de estágios por mês
   */
  applyFiltroEstagiosPorMes() {
    if (this.estadoEstagiosPorMes.length === 0) {
      this.estadoEstagiosPorMesFiltrados = [];
      this.prepareEstagiosPorMesChartData();
      return;
    }

    // Calcula valores de comparação
    const valorInicial = this.anoInicial * 100 + this.mesInicial;
    const valorFinal = this.anoFinal * 100 + this.mesFinal;

    // Filtra os dados
    this.estadoEstagiosPorMesFiltrados = this.estadoEstagiosPorMes.filter(item => {
      const valorItem = item.ano * 100 + item.semestre; // semestre contém o mês (1-12)
      return valorItem >= valorInicial && valorItem <= valorFinal;
    });

    // Atualiza o gráfico
    this.prepareEstagiosPorMesChartData();
  }

  /**
   * Prepara os dados para o gráfico de estágios por mês (separado em obrigatórios e não obrigatórios)
   */
  private prepareEstagiosPorMesChartData() {
    if (!this.estadoEstagiosPorMesFiltrados || this.estadoEstagiosPorMesFiltrados.length === 0) {
      this.chartDataEstagiosPorMesObrigatorio = null;
      this.chartDataEstagiosPorMesNaoObrigatorio = null;
      return;
    }

    try {
      // Ordena os dados por ano e mês
      let dadosOrdenados = [...this.estadoEstagiosPorMesFiltrados].sort((a, b) => {
        if (a.ano !== b.ano) return a.ano - b.ano;
        return a.semestre - b.semestre; // semestre contém o mês (1-12)
      });

      // Cria labels (meses)
      const labels = dadosOrdenados.map(item => {
        const mes = item.semestre;
        const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
        return `${meses[mes - 1]}/${item.ano}`;
      });

      // Dados para estágios OBRIGATÓRIOS
      const datasetsIniciadosObrigatorios = dadosOrdenados.map(item => item.qtdeIniciadosObrigatorios || 0);
      const datasetsFinalizadosObrigatorios = dadosOrdenados.map(item => item.qtdeFinalizadosObrigatorios || 0);

      // Dados para estágios NÃO OBRIGATÓRIOS
      const datasetsIniciadosNaoObrigatorios = dadosOrdenados.map(item => item.qtdeIniciadosNaoObrigatorios || 0);
      const datasetsFinalizadosNaoObrigatorios = dadosOrdenados.map(item => item.qtdeFinalizadosNaoObrigatorios || 0);

      // GRÁFICO DE ESTÁGIOS OBRIGATÓRIOS
      this.chartDataEstagiosPorMesObrigatorio = {
        labels: labels,
        datasets: [
          {
            label: 'Iniciados',
            data: datasetsIniciadosObrigatorios,
            borderColor: '#dc3545',
            backgroundColor: '#dc354520',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#dc3545',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointHoverRadius: 6
          },
          {
            label: 'Finalizados',
            data: datasetsFinalizadosObrigatorios,
            borderColor: '#FFBE00',
            backgroundColor: '#FFBE0020',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#FFBE00',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointHoverRadius: 6
          }
        ]
      };

      this.chartOptionsEstagiosPorMesObrigatorio = {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              font: { size: 12, weight: 'bold' },
              color: '#2c3e50',
              padding: 15
            }
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(44, 62, 80, 0.9)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(220, 53, 69, 1)',
            borderWidth: 1,
            padding: 12,
            displayColors: true
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Período (Mês/Ano)',
              color: '#2c3e50',
              font: { size: 12, weight: 'bold' }
            },
            ticks: {
              color: '#7f8c8d',
              font: { size: 11 }
            },
            grid: { color: 'rgba(127, 140, 141, 0.1)' }
          },
          y: {
            title: {
              display: true,
              text: 'Quantidade',
              color: '#2c3e50',
              font: { size: 12, weight: 'bold' }
            },
            ticks: {
              color: '#2c3e50',
              font: { size: 11 }
            },
            grid: { color: 'rgba(127, 140, 141, 0.1)' }
          }
        }
      };

      // GRÁFICO DE ESTÁGIOS NÃO OBRIGATÓRIOS
      this.chartDataEstagiosPorMesNaoObrigatorio = {
        labels: labels,
        datasets: [
          {
            label: 'Iniciados',
            data: datasetsIniciadosNaoObrigatorios,
            borderColor: '#28a745',
            backgroundColor: '#28a74520',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#28a745',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointHoverRadius: 6
          },
          {
            label: 'Finalizados',
            data: datasetsFinalizadosNaoObrigatorios,
            borderColor: '#071D41',
            backgroundColor: '#071D4120',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#071D41',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointHoverRadius: 6
          }
        ]
      };

      this.chartOptionsEstagiosPorMesNaoObrigatorio = {
        responsive: true,
        maintainAspectRatio: true,
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              font: { size: 12, weight: 'bold' },
              color: '#2c3e50',
              padding: 15
            }
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(44, 62, 80, 0.9)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(40, 167, 69, 1)',
            borderWidth: 1,
            padding: 12,
            displayColors: true
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: 'Período (Mês/Ano)',
              color: '#2c3e50',
              font: { size: 12, weight: 'bold' }
            },
            ticks: {
              color: '#7f8c8d',
              font: { size: 11 }
            },
            grid: { color: 'rgba(127, 140, 141, 0.1)' }
          },
          y: {
            title: {
              display: true,
              text: 'Quantidade',
              color: '#2c3e50',
              font: { size: 12, weight: 'bold' }
            },
            ticks: {
              color: '#2c3e50',
              font: { size: 11 }
            },
            grid: { color: 'rgba(127, 140, 141, 0.1)' }
          }
        }
      };
    } catch (error) {
      console.error('Erro ao preparar dados do gráfico de estágios por mês:', error);
      this.chartDataEstagiosPorMesObrigatorio = null;
      this.chartDataEstagiosPorMesNaoObrigatorio = null;
    }
  }

  /**
   * Prepara os dados para o gráfico de tipos de estágios
   */
  private prepareTiposEstagiosChartData() {
    if (!this.tiposEstagiosPorSemestre || this.tiposEstagiosPorSemestre.length === 0) {
      this.chartDataTiposEstagios = null;
      return;
    }

    // Ordena os dados por ano/semestre
    let dadosOrdenados = [...this.tiposEstagiosPorSemestre].sort((a, b) => {
      if (a.ano !== b.ano) return a.ano - b.ano;
      return a.semestre - b.semestre;
    });

    // Filtra pelo período selecionado
    if (this.semestreInicial && this.semestreFinal) {
      const [anoIni, semIni] = this.semestreInicial.split('/').map(Number);
      const [anoFim, semFim] = this.semestreFinal.split('/').map(Number);

      dadosOrdenados = dadosOrdenados.filter(item => {
        const valorIni = anoIni * 10 + semIni;
        const valorFim = anoFim * 10 + semFim;
        const valorAtual = item.ano * 10 + item.semestre;

        return valorAtual >= valorIni && valorAtual <= valorFim;
      });
    }

    if (dadosOrdenados.length === 0) {
      this.chartDataTiposEstagios = null;
      return;
    }

    // Cria labels (semestres)
    const labels = dadosOrdenados.map(item => `${item.ano}/${item.semestre}`);

    // Extrai todos os tipos de estágios únicos
    const tiposSet = new Set<string>();
    dadosOrdenados.forEach(item => {
      Object.keys(item.quantidadePorTipo).forEach(tipo => tiposSet.add(tipo));
    });
    const tipos = Array.from(tiposSet);

    const coresPorTipo: { [key: string]: string } = {
      'Obrigatório': '#dc3545',
      'Não Obrigatório': '#28a745',
      'Projeto de Extensao': '#FFBE00',
      'PIBIC': '#071D41',
      'PIBIS': '#17a2b8',
      'PIBITI': '#6f42c1',
      'PET': '#fd7e14',
      'Voluntario': '#20c997',
    };

    const coresPadrao = [
      '#dc3545', '#28a745', '#FFBE00', '#071D41',
      '#17a2b8', '#6f42c1', '#fd7e14', '#20c997',
      '#e83e8c', '#6c757d', '#ffc107', '#343a40'
    ];

    // Cria um dataset para cada tipo
    const datasets = tipos.map((tipo, index) => {
      const data = dadosOrdenados.map(item => item.quantidadePorTipo[tipo] || 0);
      const cor = coresPorTipo[tipo] || coresPadrao[index % coresPadrao.length];

      return {
        label: tipo,
        data: data,
        backgroundColor: cor,
        borderColor: cor,
        borderWidth: 2
      };
    });

    this.chartDataTiposEstagios = {
      labels: labels,
      datasets: datasets
    };
  }

  /**
   * Carrega dados de supervisores agrupados por empresa (Tab 4)
   */
  loadSupervisores() {
    this.isLoadingSupervisores = true;
    this.errorMessageSupervisores = '';

    this.praeService.getSupervisores().subscribe({
      next: (data: EmpresaComSupervisoresDto[]) => {
        this.empresasComSupervisores = data;
        // Prepara os dados hierárquicos para exibição na tabela
        this.empresasComSupervisoresFiltradas = this.flattenEmpresasComSupervisores(data);
        // Popula opções de filtro
        this.buildFilterOptions();
        // Constrói lista de semestres para gráfico
        this.buildSemestresGraficoOptions();
        // Prepara dados do gráfico de empresas
        this.prepareEmpresasEstagiosChartData();
        this.isLoadingSupervisores = false;
      },
      error: (error) => {
        console.error('Erro ao carregar supervisores:', error);
        this.errorMessageSupervisores = 'Erro ao carregar dados de supervisores. Tente novamente mais tarde.';
        this.isLoadingSupervisores = false;
      }
    });
  }

  private flattenEmpresasComSupervisores(empresas: EmpresaComSupervisoresDto[]): any[] {
    return empresas.map(empresa => {
      const supervisoresConsolidados = new Map<string, any>();

      empresa.supervisores.forEach((supervisor: any) => {
        const nome = supervisor.nomeSupervisor;

        if (supervisoresConsolidados.has(nome)) {
          const existente = supervisoresConsolidados.get(nome);
          existente.quantidadeEstagios += supervisor.quantidadeEstagios || 0;
        } else {
          supervisoresConsolidados.set(nome, {
            nomeSupervisor: supervisor.nomeSupervisor,
            quantidadeEstagios: supervisor.quantidadeEstagios || 0
          });
        }
      });

      return {
        ...empresa,
        supervisores: Array.from(supervisoresConsolidados.values())
      };
    });
  }

  /**
   * Popula a lista de filtros dropdown com valores únicos de empresas
   */
  private buildFilterOptions() {
    // Cria lista de empresas únicas
    const empresasUnicas = [...new Set(this.empresasComSupervisores.map(e => e.nomeEmpresa))];
    this.listaEmpresas = empresasUnicas
      .sort()
      .map(empresa => ({ label: empresa, value: empresa }));
  }

  /**
   * Constrói lista de semestres disponíveis para filtro do gráfico
   */
  private buildSemestresGraficoOptions() {
    // Extrai semestres únicos dos dados com ano - tenta obter do backend ou gera lista padrão
    const semestresSet = new Set<string>();
    const anoAtual = new Date().getFullYear();

    // Tenta extrair ano/semestre dos supervisores (se disponível)
    this.empresasComSupervisores.forEach((e: any) => {
      (e.supervisores || []).forEach((s: any) => {
        if (s.ano != null && s.semestre != null) {
          semestresSet.add(`${s.ano}/${s.semestre}`);
        } else if (s.semestre != null) {
          // Se só tem semestre, usa ano atual
          semestresSet.add(`${anoAtual}/${s.semestre}`);
        }
      });
    });

    // Se não encontrou dados com semestre, cria lista padrão
    if (semestresSet.size === 0) {
      for (let i = 1; i <= 2; i++) {
        semestresSet.add(`${anoAtual}/${i}`);
      }
    }

    // Converte para array e ordena
    this.listaSemestresGrafico = Array.from(semestresSet)
      .sort((a: string, b: string) => {
        const [anoA, semA] = a.split('/').map(Number);
        const [anoB, semB] = b.split('/').map(Number);
        if (anoA !== anoB) return anoA - anoB;
        return semA - semB;
      })
      .map(sem => ({
        label: sem,
        value: sem
      }));

    // Define os filtros iniciais como o primeiro e último semestre
    if (this.listaSemestresGrafico.length > 0) {
      this.semestreGraficoInicial = this.listaSemestresGrafico[0].value;
      this.semestreGraficoFinal = this.listaSemestresGrafico[this.listaSemestresGrafico.length - 1].value;
    }
  }

  /**
   * Aplica critérios de filtro aos dados de empresas
   * Filtra por nome de empresa selecionada
   */
  aplicarFiltrosSupervisores() {
    // Começa com os dados originais
    let dadosFiltrados = [...this.empresasComSupervisores];

    // Aplica filtro de empresa
    if (this.empresaFiltro && this.empresaFiltro.trim() !== '') {
      dadosFiltrados = dadosFiltrados.filter(empresa => empresa.nomeEmpresa === this.empresaFiltro);
    }

    // Atualiza a lista filtrada
    this.empresasComSupervisoresFiltradas = dadosFiltrados;

    // Limpa linhas expandidas quando o filtro muda
    this.expandedRows = {};
  }

  /**
   * Manipula o evento de expansão/colapso de linhas na tabela de supervisores
   * Atualiza o estado das linhas expandidas
   */
  onRowToggleSupervisores(event: any) {
    // event.data contém a linha que foi expandida/colapsada
    this.expandedRows = event.data;
  }

  /**
   * Prepara dados do gráfico de quantidade de estágios por empresa (Tab 4)
   */
  private prepareEmpresasEstagiosChartData() {
    if (!this.empresasComSupervisores || this.empresasComSupervisores.length === 0) {
      this.chartDataEmpresasEstagios = null;
      return;
    }

    try {
      let empresasComTotal = this.empresasComSupervisores.map((e: any) => {
        return {
          nomeEmpresa: e.nomeEmpresa,
          quantidadeObrigatorios: e.quantidadeObrigatorios || 0,
          quantidadeNaoObrigatorios: e.quantidadeNaoObrigatorios || 0,
          total: (e.quantidadeObrigatorios || 0) + (e.quantidadeNaoObrigatorios || 0)
        };
      });

      // Armazena a lista completa filtrada
      this.empresasEstagiosFiltradas = empresasComTotal.filter(e =>
        this.filtroGraficoEmpresas === '' ||
        e.nomeEmpresa.toLowerCase().includes(this.filtroGraficoEmpresas.toLowerCase())
      );

      // Ordena por total de estágios (decrescente) e depois por nome
      this.empresasEstagiosFiltradas.sort((a, b) => {
        if (b.total !== a.total) return b.total - a.total;
        return a.nomeEmpresa.localeCompare(b.nomeEmpresa);
      });

      // Define quais empresas mostrar no gráfico
      if (this.mostrarTop10Empresas && this.filtroGraficoEmpresas === '') {
        this.empresasEstagiosParaGrafico = this.empresasEstagiosFiltradas.slice(0, 10);
      } else {
        this.empresasEstagiosParaGrafico = this.empresasEstagiosFiltradas;
      }

      // Extrai dados para o gráfico
      const labels = this.empresasEstagiosParaGrafico.map(e => e.nomeEmpresa);
      const obrigatorios = this.empresasEstagiosParaGrafico.map(e => e.quantidadeObrigatorios);
      const naoObrigatorios = this.empresasEstagiosParaGrafico.map(e => e.quantidadeNaoObrigatorios);

      this.chartDataEmpresasEstagios = {
        labels: labels,
        datasets: [
          {
            label: 'Estágios Obrigatórios',
            data: obrigatorios,
            backgroundColor: '#dc3545',
            borderColor: '#c82333',
            borderWidth: 1,
            borderRadius: 4
          },
          {
            label: 'Estágios Não Obrigatórios',
            data: naoObrigatorios,
            backgroundColor: '#28a745',
            borderColor: '#218838',
            borderWidth: 1,
            borderRadius: 4
          }
        ]
      };

      this.chartOptionsEmpresasEstagios = {
        responsive: true,
        maintainAspectRatio: true,
        indexAxis: 'y',
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              font: { size: 12, weight: 'bold' },
              color: '#2c3e50',
              padding: 15
            }
          },
          tooltip: {
            enabled: true,
            backgroundColor: 'rgba(44, 62, 80, 0.9)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(52, 152, 219, 1)',
            borderWidth: 1,
            padding: 12,
            displayColors: true,
            callbacks: {
              afterLabel: (context: any) => {
                const dataIndex = context.dataIndex;
                const obrig = obrigatorios[dataIndex];
                const naoObrig = naoObrigatorios[dataIndex];
                const total = obrig + naoObrig;
                return `Total: ${total} estágios`;
              }
            }
          }
        },
        scales: {
          x: {
            stacked: false,
            title: {
              display: true,
              text: 'Quantidade de Estágios',
              color: '#2c3e50',
              font: { size: 12, weight: 'bold' }
            },
            ticks: {
              color: '#7f8c8d',
              font: { size: 11 }
            },
            grid: { color: 'rgba(127, 140, 141, 0.1)' }
          },
          y: {
            stacked: false,
            ticks: {
              color: '#2c3e50',
              font: { size: 11, weight: 'bold' }
            },
            grid: { display: false },
            categoryPercentage: 0.6,
            barPercentage: 0.85
          }
        }
      };
    } catch (error) {
      console.error('Erro ao preparar dados do gráfico de empresas:', error);
      this.chartDataEmpresasEstagios = null;
    }
  }

  /**
   * Aplica filtro ao gráfico de empresas e atualiza a visualização
   */
  aplicarFiltroGraficoEmpresas() {
    this.prepareEmpresasEstagiosChartData();
  }

  /**
   * Alterna entre visualizar top 10 ou todas as empresas filtradas
   */
  toggleTop10Empresas() {
    this.mostrarTop10Empresas = !this.mostrarTop10Empresas;
    this.prepareEmpresasEstagiosChartData();
  }

  /**
   * Aplica filtro de semestre ao gráfico de empresas
   */
  aplicarFiltroSemestreGrafico() {
    this.prepareEmpresasEstagiosChartData();
  }

  /**
   * Carrega as estatísticas de duração de estágios (Tab 5)
   */
  loadEstatisticasDuracao() {
    this.isLoadingDuracao = true;
    this.errorMessageDuracao = '';

    this.praeService.getEstatisticasDuracaoPorTipoEstagio().subscribe({
      next: (data: any) => {
        this.estatisticasDuracaoPorTipoEstagio = data;
        this.listaTiposEstagio = data.tiposDisponiveis.map((tipo: string) => ({
          label: tipo,
          value: tipo
        }));

        // Se houver tipos disponíveis, seleciona todos por padrão
        if (this.listaTiposEstagio.length > 0) {
          this.tiposEstagio = this.listaTiposEstagio.map(t => t.value);
          this.atualizarGraficosPorTipo();
        }

        this.isLoadingDuracao = false;
      },
      error: (error) => {
        console.error('Erro ao carregar estatísticas de duração:', error);
        this.errorMessageDuracao = 'Erro ao carregar dados de duração. Tente novamente mais tarde.';
        this.isLoadingDuracao = false;
      }
    });
  }

  /**
   * Atualiza os gráficos baseado nos tipos de estágio selecionados
   * Agrega os dados de todos os tipos selecionados
   */
  atualizarGraficosPorTipo() {
    if (this.tiposEstagio.length === 0 || !this.estatisticasDuracaoPorTipoEstagio) {
      return;
    }

    // Coleta dados de obrigatórios e não obrigatórios de todos os tipos selecionados
    const statsObrigatorios = this.coletarEstatisticasAgregadas('obrigatorios');
    const statsNaoObrigatorios = this.coletarEstatisticasAgregadas('naoObrigatorios');

    // Cria objeto com estatísticas agregadas
    this.estatisticasCurrentes = {
      obrigatorios: statsObrigatorios,
      naoObrigatorios: statsNaoObrigatorios
    };

    this.gerarGraficoBoxPlotObrigatorio(statsObrigatorios);
    this.gerarGraficoBoxPlotNaoObrigatorio(statsNaoObrigatorios);
  }

  /**
   * Coleta e agrega estatísticas de todos os tipos selecionados
   * @param tipo 'obrigatorios' ou 'naoObrigatorios'
   */
  private coletarEstatisticasAgregadas(tipo: 'obrigatorios' | 'naoObrigatorios') {
    let totalEstagios = 0;
    let mediaTotal = 0;
    let minimaGeral = Number.MAX_VALUE;
    let maximaGeral = 0;

    // Itera por cada tipo selecionado e coleta os dados
    for (const tipoEstagio of this.tiposEstagio) {
      const estatisticas = this.estatisticasDuracaoPorTipoEstagio.estatisticasPorTipo[tipoEstagio];
      if (estatisticas && estatisticas[tipo]) {
        const stats = estatisticas[tipo];
        totalEstagios += stats.totalEstagios;
        minimaGeral = Math.min(minimaGeral, stats.duracaoMinima);
        maximaGeral = Math.max(maximaGeral, stats.duracaoMaxima);

        // Calcula duração total para média agregada
        // Trata ambas as variações de propriedade (com e sem typo do backend)
        const media = stats.duracaoMedia !== undefined ? stats.duracaoMedia : stats.durracaoMedia;
        mediaTotal += (media || 0) * stats.totalEstagios;
      }
    }

    // Calcula a média agregada
    const mediaAgregada = totalEstagios > 0 ? mediaTotal / totalEstagios : 0;

    // Se nenhum tipo foi selecionado com dados, retorna valores padrão
    if (totalEstagios === 0) {
      return {
        duracaoMedia: 0,
        duracaoMinima: 0,
        duracaoMaxima: 0,
        primeiroQuartil: 0,
        mediana: 0,
        terceiroQuartil: 0,
        totalEstagios: 0
      };
    }

    // Calcula percentis com base nas durações agregadas
    // Para simplificar, usamos aproximação com base nas estatísticas
    const q1 = Math.round(minimaGeral + (maximaGeral - minimaGeral) * 0.25);
    const mediana = Math.round(minimaGeral + (maximaGeral - minimaGeral) * 0.5);
    const q3 = Math.round(minimaGeral + (maximaGeral - minimaGeral) * 0.75);

    return {
      duracaoMedia: mediaAgregada,
      duracaoMinima: minimaGeral === Number.MAX_VALUE ? 0 : minimaGeral,
      duracaoMaxima: maximaGeral,
      primeiroQuartil: q1,
      mediana: mediana,
      terceiroQuartil: q3,
      totalEstagios: totalEstagios
    };
  }

  /**
   * Seleciona todos os tipos de estágio disponíveis
   */
  selecionarTodosTiposEstagio() {
    if (this.listaTiposEstagio.length > 0) {
      this.tiposEstagio = this.listaTiposEstagio.map(t => t.value);
      this.atualizarGraficosPorTipo();
    }
  }

  /**
   * Limpa a seleção de tipos de estágio
   */
  limparFiltroTiposEstagio() {
    this.tiposEstagio = [];
    this.estatisticasCurrentes = null;
  }

  /**
   * Gera o gráfico Box Plot para estágios OBRIGATÓRIOS
   */
  private gerarGraficoBoxPlotObrigatorio(data: EstatisticasDuracaoEstagioDto) {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    setTimeout(() => {
      if (this.boxplotCanvasRefObrigatorio && this.boxplotCanvasRefObrigatorio.nativeElement) {
        const ctx = this.boxplotCanvasRefObrigatorio.nativeElement.getContext('2d');
        if (ctx) {
          if (this.chartBoxPlotObrigatorio) {
            this.chartBoxPlotObrigatorio.destroy();
          }

          this.chartBoxPlotObrigatorio = new Chart(ctx, {
            type: 'boxplot' as any,
            data: {
              labels: ['Estágios Obrigatórios'],
              datasets: [
                {
                  label: 'Durações (dias)',
                  data: [
                    {
                      min: data.duracaoMinima,
                      q1: data.primeiroQuartil,
                      median: data.mediana,
                      q3: data.terceiroQuartil,
                      max: data.duracaoMaxima
                    }
                  ],
                  backgroundColor: 'rgba(39, 174, 96, 0.6)',
                  borderColor: 'rgba(22, 160, 133, 1)',
                  borderWidth: 2,
                  medianColor: 'rgba(192, 57, 43, 1)',
                  itemRadius: 5,
                  itemBackgroundColor: 'rgba(22, 160, 133, 0.9)',
                  itemBorderColor: 'rgba(22, 160, 133, 1)',
                  itemBorderWidth: 1,
                  itemStyle: 'circle'
                }
              ]
            },
            options: {
              indexAxis: 'y',
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: {
                  display: true,
                  position: 'top',
                  labels: {
                    font: {
                      size: 12,
                      weight: 'bold'
                    },
                    color: '#2c3e50',
                    padding: 15
                  }
                },
                title: {
                  display: false
                },
                tooltip: {
                  enabled: true,
                  backgroundColor: 'rgba(44, 62, 80, 0.9)',
                  titleColor: '#fff',
                  bodyColor: '#fff',
                  borderColor: 'rgba(39, 174, 96, 1)',
                  borderWidth: 1,
                  padding: 12,
                  displayColors: false,
                  callbacks: {
                    title: () => 'Estatísticas (Obrigatórios)',
                    label: (context: any) => {
                      const item = context.raw;
                      return [
                        `Mínima: ${item.min} dias`,
                        `Q1 (25%): ${item.q1} dias`,
                        `Mediana: ${item.median} dias`,
                        `Q3 (75%): ${item.q3} dias`,
                        `Máxima: ${item.max} dias`,
                        `Total: ${data.totalEstagios} estágios`
                      ];
                    }
                  }
                }
              },
              scales: {
                x: {
                  type: 'linear',
                  position: 'bottom',
                  title: {
                    display: true,
                    text: 'Duração (dias)',
                    color: '#2c3e50',
                    font: {
                      size: 12,
                      weight: 'bold'
                    }
                  },
                  min: 0,
                  max: data.duracaoMaxima + 100,
                  ticks: {
                    color: '#7f8c8d',
                    font: {
                      size: 11
                    }
                  },
                  grid: {
                    color: 'rgba(127, 140, 141, 0.1)'
                  }
                },
                y: {
                  display: true,
                  ticks: {
                    color: '#2c3e50',
                    font: {
                      size: 12,
                      weight: 'bold'
                    }
                  },
                  grid: {
                    display: false
                  }
                }
              }
            }
          });
        }
      }
    }, 100);
  }

  /**
   * Gera o gráfico Box Plot para estágios NÃO OBRIGATÓRIOS
   */
  private gerarGraficoBoxPlotNaoObrigatorio(data: EstatisticasDuracaoEstagioDto) {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    setTimeout(() => {
      if (this.boxplotCanvasRefNaoObrigatorio && this.boxplotCanvasRefNaoObrigatorio.nativeElement) {
        const ctx = this.boxplotCanvasRefNaoObrigatorio.nativeElement.getContext('2d');
        if (ctx) {
          if (this.chartBoxPlotNaoObrigatorio) {
            this.chartBoxPlotNaoObrigatorio.destroy();
          }

          this.chartBoxPlotNaoObrigatorio = new Chart(ctx, {
            type: 'boxplot' as any,
            data: {
              labels: ['Estágios Não Obrigatórios'],
              datasets: [
                {
                  label: 'Durações (dias)',
                  data: [
                    {
                      min: data.duracaoMinima,
                      q1: data.primeiroQuartil,
                      median: data.mediana,
                      q3: data.terceiroQuartil,
                      max: data.duracaoMaxima
                    }
                  ],
                  backgroundColor: 'rgba(155, 89, 182, 0.6)',
                  borderColor: 'rgba(142, 68, 173, 1)',
                  borderWidth: 2,
                  medianColor: 'rgba(192, 57, 43, 1)',
                  itemRadius: 5,
                  itemBackgroundColor: 'rgba(142, 68, 173, 0.9)',
                  itemBorderColor: 'rgba(142, 68, 173, 1)',
                  itemBorderWidth: 1,
                  itemStyle: 'circle'
                }
              ]
            },
            options: {
              indexAxis: 'y',
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: {
                  display: true,
                  position: 'top',
                  labels: {
                    font: {
                      size: 12,
                      weight: 'bold'
                    },
                    color: '#2c3e50',
                    padding: 15
                  }
                },
                title: {
                  display: false
                },
                tooltip: {
                  enabled: true,
                  backgroundColor: 'rgba(44, 62, 80, 0.9)',
                  titleColor: '#fff',
                  bodyColor: '#fff',
                  borderColor: 'rgba(155, 89, 182, 1)',
                  borderWidth: 1,
                  padding: 12,
                  displayColors: false,
                  callbacks: {
                    title: () => 'Estatísticas (Não Obrigatórios)',
                    label: (context: any) => {
                      const item = context.raw;
                      return [
                        `Mínima: ${item.min} dias`,
                        `Q1 (25%): ${item.q1} dias`,
                        `Mediana: ${item.median} dias`,
                        `Q3 (75%): ${item.q3} dias`,
                        `Máxima: ${item.max} dias`,
                        `Total: ${data.totalEstagios} estágios`
                      ];
                    }
                  }
                }
              },
              scales: {
                x: {
                  type: 'linear',
                  position: 'bottom',
                  title: {
                    display: true,
                    text: 'Duração (dias)',
                    color: '#2c3e50',
                    font: {
                      size: 12,
                      weight: 'bold'
                    }
                  },
                  min: 0,
                  max: data.duracaoMaxima + 100,
                  ticks: {
                    color: '#7f8c8d',
                    font: {
                      size: 11
                    }
                  },
                  grid: {
                    color: 'rgba(127, 140, 141, 0.1)'
                  }
                },
                y: {
                  display: true,
                  ticks: {
                    color: '#2c3e50',
                    font: {
                      size: 12,
                      weight: 'bold'
                    }
                  },
                  grid: {
                    display: false
                  }
                }
              }
            }
          });
        }
      }
    }, 100);
  }
}
