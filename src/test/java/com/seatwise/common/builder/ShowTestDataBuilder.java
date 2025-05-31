package com.seatwise.common.builder;

import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTestDataBuilder {

  @Autowired private ShowRepository showRepository;
  private String title;
  private String description;
  private ShowType type;

  public ShowTestDataBuilder(ShowRepository showRepository) {
    this.showRepository = showRepository;
  }

  public ShowTestDataBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public ShowTestDataBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public ShowTestDataBuilder withType(ShowType type) {
    this.type = type;
    return this;
  }

  public Show build() {
    Show show = new Show(title, description, type);
    return showRepository.save(show);
  }
}
