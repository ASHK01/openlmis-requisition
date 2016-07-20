package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "facilities", schema = "referencedata")
@NoArgsConstructor
public class Facility extends BaseEntity {

  public static final String TEXT = "text";

  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "geographiczoneid", nullable = false)
  @Getter
  @Setter
  private GeographicZone geographicZone;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "typeid", nullable = false)
  @Getter
  @Setter
  private FacilityType type;

  @ManyToOne
  @JoinColumn(name = "operatedbyid")
  @Getter
  @Setter
  private FacilityOperator operator;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean active;

  @Getter
  @Setter
  private Date goLiveDate;

  @Getter
  @Setter
  private Date goDownDate;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String comment;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean enabled;

  @Getter
  @Setter
  private Boolean openLmisAccessible;

  @OneToOne
  @JoinColumn(name = "stockInventoryId")
  @Getter
  @Setter
  private StockInventory stockInventory;

  @OneToMany
  @JoinColumn(name = "programsId")
  @Getter
  @Setter
  private List<Program> supportedPrograms;
}
