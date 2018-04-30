@api:ricerca clienti
@product: wscs
@ambiente: svil

Feature: REQ-F 23
  Background:
  	Given debug is deactive
    Given protocol is http
    And host is webwassvc-test.servizi.gr-u.it
    And path is /DsiWatsonSearchCrmServiceWeb/rest/crm/ricerca/clienti
    And UserId is E3060101
    And UserPwd is Unipol97
    
  @component: search
  @operation: /ricerca/clienti
  Scenario: Ricerca di Gruppo REQ-F 23
  
    Given query Mario Rossi
    And risultati in pagina 20
    Then response code 200
    And the response match REQ-F 23
      
    Given query Grazian Michel
    And pagina 1
    And risultati in pagina 10
    Then response code 200
    And the response match REQ-F 23
    
    