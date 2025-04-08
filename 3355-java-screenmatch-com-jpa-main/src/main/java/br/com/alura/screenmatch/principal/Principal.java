package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.Repository.SerieRepository;
import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private Optional<Serie> serieBusca;

    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - listar séries buscadas
                    4 - Buscar série por titulo
                    5 - Buscar series por ator
                    6 - Top 5 series
                    7 - Buscar por categorias
                    8 - Buscar série por temporada e avaliação
                    9 - Buscar episódio po trecho
                    10 - Top 5 episodios por série
                    11 - Episodios a partir de uma data
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    top5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSerieTemporadasAnsAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    top5Episodios();
                    break;
                case 11:
                    buscarEpisodioData();
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarEpisodioData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            System.out.print("Digite a data minima qudeseja começa a ver a série: ");
            int data = leitura.nextInt();
            Serie serie = serieBusca.get();
            List<Episodio> episodioData = repositorio.episodiosPorData(serie, data);
            episodioData.forEach(System.out::println);
        }
    }

    private void top5Episodios() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodio = repositorio.topEpisodiosPorSerie(serie);
            topEpisodio.forEach(System.out::println);
        }
    }

    private void buscarEpisodioPorTrecho() {
        System.out.print("Digite um trecho para busca episódio :");
        String trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
        System.out.println("Episodios");
        episodiosEncontrados.forEach(System.out::println);
    }

    private void buscarSerieTemporadasAnsAvaliacao() {
        System.out.print("Digite a quantidade de temporadas que você deseja assistir: ");
        var totalTemporadas = leitura.nextInt();
        System.out.println("Digite minima avaliação que deseja: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas,avaliacao);
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo()+" - "+s.getAvaliacao()+" - "+s.getGenero()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja pesquisar series por qual categoria");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriePorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria "+nomeGenero);
        seriePorCategoria.forEach(System.out::println);
    }

    private void top5Series() {
        List<Serie>serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s -> System.out.println(s.getTitulo()+" - "+s.getAvaliacao()));
    }

    private void buscarSeriesPorAtor() {
        System.out.print("Digite o nome do ator para var suas series:");
        String nomeAtor = leitura.nextLine();
        System.out.print("Avaliações a partir de qual valor: ");
        double avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor,avaliacao);
        System.out.println("Séries em que o "+nomeAtor+" trabalhou");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo()+" - "+s.getAvaliacao()));
    }

    private void buscarSeriePorTitulo() {
        System.out.print("Digite o nome da série:");
        String nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBusca.isPresent()){
            System.out.println("Dados da serie"+serieBusca.get());

        }else{
            System.out.println("Nenhuma série encontrada.");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream().sorted(Comparator.comparing(Serie::getGenero)).forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }


    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.print("Escolha uma serie pelo nome:");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> firstSerie =repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (firstSerie.isPresent()) {
            var serieEncontrada = firstSerie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e, serieEncontrada))) // Passando a série aqui
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada.");
        }
    }
}