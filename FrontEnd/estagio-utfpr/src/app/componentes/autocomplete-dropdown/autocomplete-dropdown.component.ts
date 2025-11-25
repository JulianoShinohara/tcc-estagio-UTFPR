import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef
} from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-autocomplete-dropdown',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './autocomplete-dropdown.component.html',
  styleUrls: ['./autocomplete-dropdown.component.scss']
})
export class AutocompleteDropdownComponent implements OnInit, OnDestroy {
  @Input() placeholder: string = 'Digite ou selecione o nome do orientador...';
  @Input() label: string = '';
  @Input() loadOptionsFunction!: () => Promise<string[]>;
  @Input() minCharacters: number = 0;
  @Input() debounceTime: number = 200;

  @Output() orientadorSelected = new EventEmitter<string>();
  @Output() inputCleared = new EventEmitter<void>();

  @ViewChild('inputElement') inputElement!: ElementRef<HTMLInputElement>;

  inputValue: string = '';
  selectedValue: string | null = null;
  allOptions: string[] = [];
  filteredOptions: string[] = [];
  showDropdown: boolean = false;
  isFocused: boolean = false;
  isLoading: boolean = false;
  noResultsFound: boolean = false;
  errorMessage: string = '';
  highlightedIndex: number = -1;
  optionsLoaded: boolean = false;

  private searchSubject = new Subject<string>();
  private subscription = new Subscription();

  ngOnInit(): void {
    this.setupSearch();
    this.loadAllOptions();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private async loadAllOptions(): Promise<void> {
    try {
      if (this.loadOptionsFunction) {
        this.isLoading = true;
        this.errorMessage = '';
        this.allOptions = await this.loadOptionsFunction();
        this.optionsLoaded = true;
      }
    } catch (error) {
      this.handleLoadError(error);
    } finally {
      this.isLoading = false;
    }
  }

  private setupSearch(): void {
    const searchSubscription = this.searchSubject
      .pipe(
        debounceTime(this.debounceTime),
        distinctUntilChanged()
      )
      .subscribe(term => {
        this.filterOptions(term);
      });

    this.subscription.add(searchSubscription);
  }

  private filterOptions(term: string): void {
    if (!this.optionsLoaded) {
      this.filteredOptions = [];
      this.noResultsFound = false;
      return;
    }

    const searchTerm = term.toLowerCase().trim();

    if (searchTerm.length === 0) {
      this.filteredOptions = [...this.allOptions];
      this.noResultsFound = false;
    } else {
      this.filteredOptions = this.allOptions.filter(option =>
        option.toLowerCase().includes(searchTerm)
      );
      this.noResultsFound = this.filteredOptions.length === 0;
    }

    this.highlightedIndex = -1;
  }

  private handleLoadError(error: any): void {
    this.errorMessage = 'Erro ao carregar lista de orientadores. Tente recarregar a página.';
    this.allOptions = [];
    this.filteredOptions = [];
    this.optionsLoaded = false;
    console.error('Erro ao carregar opções:', error);
  }

  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.inputValue = target.value;

    if (this.selectedValue && this.inputValue !== this.selectedValue) {
      this.selectedValue = null;
    }

    this.searchSubject.next(this.inputValue);
  }

  onFocus(): void {
    this.isFocused = true;

    if (this.optionsLoaded) {
      if (this.filteredOptions.length === 0 && this.inputValue.trim().length === 0) {
        this.filteredOptions = [...this.allOptions];
      }
      this.showDropdown = true;
    }
  }

  onBlur(): void {
    setTimeout(() => {
      this.isFocused = false;
      this.showDropdown = false;
    }, 200);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (!this.showDropdown || this.filteredOptions.length === 0) {
      return;
    }

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.highlightedIndex = Math.min(
          this.highlightedIndex + 1,
          this.filteredOptions.length - 1
        );
        break;

      case 'ArrowUp':
        event.preventDefault();
        this.highlightedIndex = Math.max(this.highlightedIndex - 1, 0);
        break;

      case 'Enter':
        event.preventDefault();
        if (this.highlightedIndex >= 0 && this.filteredOptions[this.highlightedIndex]) {
          this.selectOption(this.filteredOptions[this.highlightedIndex]);
        }
        break;

      case 'Escape':
        this.showDropdown = false;
        this.inputElement.nativeElement.blur();
        break;
    }
  }

  selectOption(nomeOrientador: string): void {
    this.selectedValue = nomeOrientador;
    this.inputValue = nomeOrientador;
    this.showDropdown = false;
    this.highlightedIndex = -1;
    this.errorMessage = '';

    this.orientadorSelected.emit(nomeOrientador);
  }

  clearSelection(): void {
    this.selectedValue = null;
    this.inputValue = '';
    this.filteredOptions = [...this.allOptions];
    this.showDropdown = false;
    this.highlightedIndex = -1;
    this.errorMessage = '';
    this.noResultsFound = false;

    this.inputCleared.emit();
    this.inputElement.nativeElement.focus();
  }

  highlightMatch(text: string): string {
    if (!this.inputValue || this.inputValue.trim().length === 0) {
      return text;
    }

    const regex = new RegExp(`(${this.escapeRegex(this.inputValue)})`, 'gi');
    return text.replace(regex, '<span class="highlight">$1</span>');
  }

  private escapeRegex(string: string): string {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  public reset(): void {
    this.clearSelection();
  }

  public setValue(nomeOrientador: string): void {
    this.selectedValue = nomeOrientador;
    this.inputValue = nomeOrientador;
  }

  public async reloadOptions(): Promise<void> {
    this.optionsLoaded = false;
    this.allOptions = [];
    this.filteredOptions = [];
    await this.loadAllOptions();
  }

  public get hasOptions(): boolean {
    return this.optionsLoaded && this.allOptions.length > 0;
  }

  public get totalOptions(): number {
    return this.allOptions.length;
  }
}